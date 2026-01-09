from flask import Flask, request, jsonify
from flask_cors import CORS
from langchain_core.prompts import ChatPromptTemplate
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_classic.chains import create_retrieval_chain
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_tavily import TavilySearch
from langchain.agents import create_agent
from pydantic import BaseModel, Field
from typing import Type, Optional
from langchain.tools import BaseTool
from langchain_core.callbacks import CallbackManagerForToolRun
import os

import firebase_admin
from firebase_admin import credentials, firestore
from pathlib import Path

app = Flask(__name__)
CORS(app)

# FIREBASE SETUP 
key_path = Path(__file__).parent / "firebase-key.json"
print("Using key path:", key_path.resolve())

if not key_path.exists():
    raise FileNotFoundError(f"Không tìm thấy firebase key tại {key_path.resolve()}")

cred = credentials.Certificate(str(key_path.resolve()))
firebase_admin.initialize_app(cred)
db = firestore.client()
print("Firebase connected successfully")

# SETUP CHATBOT 
GOOGLE_API_KEY = "AIzaSyC8ifm4SoBGmQ_dbsq6_mMMZwjmCbrJGjg"
TAVILY_API_KEY = "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"
if not GOOGLE_API_KEY or not TAVILY_API_KEY:
    print("Warning: missing GOOGLE_API_KEY or TAVILY_API_KEY in environment variables")

# Initialize LLM
llm = ChatGoogleGenerativeAI(
    api_key=GOOGLE_API_KEY,
    model="gemini-2.5-flash",
    temperature=0,
    max_tokens=None,
)

# Load documents
script_dir = os.path.dirname(os.path.abspath(__file__))
documents = []
for filename in os.listdir(script_dir):
    if not filename.endswith('.txt'):
        continue
    file_path = os.path.join(script_dir, filename)
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
            documents.append(
                Document(
                    page_content=content,
                    metadata={'source': filename}
                )
            )
    except Exception as exc:
        print(f"Warning: could not load {filename}: {exc}")

if not documents:
    print("Warning: no .txt documents were loaded into the knowledge base")

# Split documents
text_splitter = RecursiveCharacterTextSplitter(
    chunk_size=1200,
    chunk_overlap=200
)
all_splits = text_splitter.split_documents(documents)

# Setup embeddings
model_name = "sentence-transformers/static-similarity-mrl-multilingual-v1"
model_kwargs = {'device': 'cpu'}
encode_kwargs = {'normalize_embeddings': False}

hf_embedding = HuggingFaceEmbeddings(
    model_name=model_name,
    model_kwargs=model_kwargs,
    encode_kwargs=encode_kwargs
)

# Create vector store
vectorstore = Chroma.from_documents(
    documents=all_splits,
    embedding=hf_embedding,
    collection_name="chillstay_docs"
)

# Create prompt template for fast RAG
system_prompt = """Bạn là trợ lý ảo của ứng dụng ChillStay - hệ thống đặt phòng khách sạn, homestay, căn hộ du lịch.
Hãy trả lời câu hỏi của người dùng dựa trên thông tin được cung cấp.
Nếu thông tin không đủ, hãy trả lời dựa trên kiến thức của bạn về ứng dụng đặt phòng.
Trả lời ngắn gọn, rõ ràng, thân thiện bằng tiếng Việt."""

prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    ("human", "Context:\n{context}\n\nCâu hỏi: {input}")
])

#TOOLS

class RetriveInput(BaseModel):
    query: str = Field("", description="Search query string entered by the user")

class RetrieveTool(BaseTool):
    name: str = 'chillstay_knowledge_base'
    description: str = """Công cụ này chứa toàn bộ kiến thức về ứng dụng Chillstay.
    Sử dụng công cụ này khi người dùng hỏi về:
    - Hướng dẫn đăng ký, đăng nhập
    - Cách đặt phòng, tìm kiếm
    - Chính sách thanh toán, hủy phòng
    - Các tính năng của app
    - Xử lý lỗi
    
    LUÔN sử dụng công cụ này ĐẦU TIÊN khi hỏi về Chillstay."""
    
    args_schema: Type[BaseModel] = RetriveInput
    return_direct: bool = False
    _vector_store: Type[Chroma]
    _k: int

    def __init__(self, vectorstore, **kwargs) -> None:
        super().__init__(**kwargs)
        self._vector_store = vectorstore
        self._k = kwargs.get("k", 4)

    def _run(
        self,
        query: str,
        run_manager: Optional[CallbackManagerForToolRun] = None,
    ) -> str:
        results = self._vector_store.similarity_search(query, k=self._k)
        documents = [doc.page_content for doc in results]
        return "\n\n".join(documents)

chillstay_tool = RetrieveTool(vectorstore=vectorstore, k=4)

# Search tool
search_web_tool = TavilySearch(
    tavily_api_key=TAVILY_API_KEY,
    max_results=2,
    topic="general",
)

# --- Create agent 
agent = create_agent(
    model=llm,
    tools=[chillstay_tool, search_web_tool],
)

# FIREBASE HELPER FUNCTIONS

def get_chat_history(session_id: str, limit: int = 50):
    """Lấy lịch sử chat từ Firebase"""
    if not db:
        return []
    
    try:
        # Lấy messages từ Firestore, sắp xếp theo timestamp
        messages_ref = db.collection('chat_sessions').document(session_id).collection('messages')
        messages = messages_ref.order_by('timestamp').limit(limit).stream()
        
        history = []
        for msg in messages:
            data = msg.to_dict()
            history.append({
                "role": data.get("role"),
                "content": data.get("content")
            })
        
        return history
    except Exception as e:
        print(f"Error getting chat history: {e}")
        return []

def save_message(session_id: str, role: str, content: str):
    """Lưu tin nhắn vào Firebase"""
    if not db:
        return False
    
    try:
        # Lưu message vào subcollection
        messages_ref = db.collection('chat_sessions').document(session_id).collection('messages')
        messages_ref.add({
            'role': role,
            'content': content,
            'timestamp': firestore.SERVER_TIMESTAMP
        })
        
        # Cập nhật metadata của session
        session_ref = db.collection('chat_sessions').document(session_id)
        session_ref.set({
            'last_updated': firestore.SERVER_TIMESTAMP,
            'message_count': firestore.Increment(1)
        }, merge=True)
        
        return True
    except Exception as e:
        print(f"Error saving message: {e}")
        return False

def clear_chat_history(session_id: str):
    """Xóa lịch sử chat từ Firebase"""
    if not db:
        return False
    
    try:
        # Xóa tất cả messages trong subcollection
        messages_ref = db.collection('chat_sessions').document(session_id).collection('messages')
        messages = messages_ref.stream()
        
        for msg in messages:
            msg.reference.delete()
        
        # Xóa metadata
        session_ref = db.collection('chat_sessions').document(session_id)
        session_ref.delete()
        
        return True
    except Exception as e:
        print(f"Error clearing chat: {e}")
        return False

# API ENDPOINTS

@app.route('/api/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json(force=True, silent=True) or {}
        user_message = data.get('message', '').strip()
        session_id = data.get('session_id', 'default')
        
        if not user_message:
            return jsonify({
                'error': 'Message cannot be empty',
                'status': 'error'
            }), 400
        
        # Lấy lịch sử từ Firebase
        history = get_chat_history(session_id)
        
        # Tạo messages cho agent
        messages = history.copy()
        messages.append({"role": "user", "content": user_message})
        
        # Gọi agent
        result = agent.invoke({"messages": messages})
        
        # Xử lý response từ agent - IMPROVED VERSION
        bot_reply = ""
        
        try:
            # Trường hợp 1: Result là dict với key 'messages'
            if isinstance(result, dict) and "messages" in result:
                messages_list = result["messages"]
                if isinstance(messages_list, list) and len(messages_list) > 0:
                    # Lấy message cuối cùng (response của AI)
                    last_message = messages_list[-1]
                    
                    # Xử lý nếu là object có attribute 'content'
                    if hasattr(last_message, 'content'):
                        bot_reply = str(last_message.content)
                    # Xử lý nếu là dict
                    elif isinstance(last_message, dict):
                        bot_reply = last_message.get('content', str(last_message))
                    else:
                        bot_reply = str(last_message)
            
            # Trường hợp 2: Result có key 'output' hoặc 'response'
            elif isinstance(result, dict):
                bot_reply = result.get('output') or result.get('response') or str(result)
            
            # Trường hợp 3: Result là string hoặc object khác
            else:
                bot_reply = str(result)
            
            # Đảm bảo luôn có response, không để trống
            if not bot_reply or bot_reply.strip() == "":
                bot_reply = "Xin lỗi, tôi không thể tạo câu trả lời phù hợp. Vui lòng thử lại."
                
        except Exception as parse_error:
            print(f"Error parsing agent response: {parse_error}")
            print(f"Raw result: {result}")
            bot_reply = "Đã xảy ra lỗi khi xử lý câu trả lời. Vui lòng thử lại."
        
        # Lưu messages vào Firebase
        save_message(session_id, "user", user_message)
        save_message(session_id, "assistant", bot_reply)
        
        return jsonify({
            'response': bot_reply,
            'session_id': session_id,
            'status': 'success'
        })
    
    except Exception as e:
        print(f"Error: {str(e)}")
        import traceback
        traceback.print_exc()  # In full traceback để debug
        return jsonify({
            'error': f'Internal server error: {str(e)}',
            'status': 'error'
        }), 500

@app.route('/api/chat/clear', methods=['POST'])
def clear_chat():
    try:
        data = request.get_json()
        session_id = data.get('session_id', 'default')
        
        # Xóa lịch sử từ Firebase
        clear_chat_history(session_id)
        
        return jsonify({
            'message': 'Chat history cleared',
            'status': 'success'
        })
    
    except Exception as e:
        return jsonify({
            'error': str(e),
            'status': 'error'
        }), 500

@app.route('/api/health', methods=['GET'])
def health():
    """Enhanced health check với thông tin chi tiết về RAG system"""
    
    # documents
    doc_info = {
        'total_documents_loaded': len(documents),
        'document_sources': [doc.metadata.get('source') for doc in documents],
        'total_characters': sum(len(doc.page_content) for doc in documents)
    }
    
    # chunking
    chunk_info = {
        'total_chunks': len(all_splits),
        'chunk_size': 1200,
        'chunk_overlap': 200,
        'avg_chunk_length': sum(len(chunk.page_content) for chunk in all_splits) // len(all_splits) if all_splits else 0,
        'min_chunk_length': min(len(chunk.page_content) for chunk in all_splits) if all_splits else 0,
        'max_chunk_length': max(len(chunk.page_content) for chunk in all_splits) if all_splits else 0
    }
    
    # vector store
    vector_info = {
        'vector_count': vectorstore._collection.count(),
        'embedding_model': model_name,
        'embedding_dimension': 768,
        'collection_name': 'chillstay_docs',
        'device': model_kwargs['device']
    }
    
    # ✅ FIX: Thêm retrieval_info bị thiếu
    retrieval_info = {
        'retrieval_k': 4,
        'similarity_metric': 'cosine'
    }
    
    # tools
    tools_info = {
        'available_tools': [
            {
                'name': 'chillstay_knowledge_base',
                'type': 'retrieval',
                'status': 'active'
            },
            {
                'name': 'tavily_search',
                'type': 'web_search',
                'status': 'active' if TAVILY_API_KEY else 'inactive'
            }
        ]
    }
    
    return jsonify({
        'status': 'healthy',
        'model': {
            'llm': 'gemini-2.5-flash',
            'temperature': 0
        },
        'rag_system': {
            'documents': doc_info,
            'chunking': chunk_info,
            'vector_store': vector_info,
            'retrieval': retrieval_info
        },
        'tools': tools_info,
        'firebase_connected': db is not None,
        'server': {
            'host': '0.0.0.0',
            'port': 5000
        }
    })

if __name__ == '__main__':
    print("Starting ChillStay Chatbot API...")
    print("Vector store initialized with documents")
    print("Agent ready with tools")
    print(f"Firebase status: {'✓ Connected' if db else '✗ Not connected'}")
    print("Server running on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=False)