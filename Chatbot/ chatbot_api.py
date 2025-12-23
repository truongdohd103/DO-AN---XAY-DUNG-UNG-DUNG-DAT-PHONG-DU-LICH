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

app = Flask(__name__)
CORS(app)

# ==================== SETUP CHATBOT ====================
# setx GOOGLE_API_KEY "xxx"  (Windows)
GOOGLE_API_KEY = "AIzaSyD22V8HmqM7cX3AfIJW5yH-TkSqVY2mmjg"
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

# Load documents (resolve relative to this file so cwd changes won't break it)
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
        # Log and continue if one file fails to load
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

# Create retriever for fast RAG (không dùng agent)
retriever = vectorstore.as_retriever(search_kwargs={"k": 4})

# Create prompt template for fast RAG
system_prompt = """Bạn là trợ lý ảo của ứng dụng ChillStay - hệ thống đặt phòng khách sạn, homestay, căn hộ du lịch.
Hãy trả lời câu hỏi của người dùng dựa trên thông tin được cung cấp.
Nếu thông tin không đủ, hãy trả lời dựa trên kiến thức của bạn về ứng dụng đặt phòng.
Trả lời ngắn gọn, rõ ràng, thân thiện bằng tiếng Việt."""

prompt = ChatPromptTemplate.from_messages([
    ("system", system_prompt),
    ("human", "Context:\n{context}\n\nCâu hỏi: {input}")
])

# Create fast RAG chain (không dùng agent, nhanh hơn nhiều)
document_chain = create_stuff_documents_chain(llm, prompt)
fast_rag_chain = create_retrieval_chain(retriever, document_chain)

# ==================== TOOLS ====================

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

# --- Create agent using langchain.agents.create_agent (API mới)
agent = create_agent(
    model=llm,
    tools=[chillstay_tool, search_web_tool],
    # bạn có thể thêm response_format, max_iterations... nếu cần
)

# ==================== SESSION MANAGEMENT ====================
chat_sessions = {}

# ==================== API ENDPOINTS ====================

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
        
        if session_id not in chat_sessions:
            chat_sessions[session_id] = []
        
        history = chat_sessions[session_id]
        
        # messages định dạng theo LangChain: [{role, content}, ...]
        messages = history.copy()
        messages.append({"role": "user", "content": user_message})
        
        # gọi agent: invoke với payload chứa "messages"
        result = agent.invoke({"messages": messages})
        # result có thể chứa khóa "messages" (list) hoặc "structured_response"
        bot_reply = None
        if isinstance(result, dict):
            msgs = result.get("messages") or result.get("response") or result.get("structured_response")
            # nếu là list of dicts
            if isinstance(msgs, list) and len(msgs) > 0:
                last = msgs[-1]
                # last có thể là object Message hoặc dict
                bot_reply = getattr(last, "content", None) or last.get("content", None) or str(last)
            else:
                # fallback: convert result thành string
                bot_reply = str(result)
        else:
            bot_reply = str(result)
        
        # lưu history
        history.append({"role": "user", "content": user_message})
        history.append({"role": "assistant", "content": bot_reply})
        chat_sessions[session_id] = history
        
        return jsonify({
            'response': bot_reply,
            'session_id': session_id,
            'status': 'success'
        })
    
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({
            'error': f'Internal server error: {str(e)}',
            'status': 'error'
        }), 500

@app.route('/api/chat/fast', methods=['POST'])
def chat_fast():
    """
    Endpoint nhanh hơn: dùng RAG trực tiếp, không qua agent.
    Phù hợp cho câu hỏi về ChillStay (hướng dẫn, chính sách, tính năng...)
    """
    try:
        data = request.get_json(force=True, silent=True) or {}
        user_message = data.get('message', '').strip()
        session_id = data.get('session_id', 'default')
        
        if not user_message:
            return jsonify({
                'error': 'Message cannot be empty',
                'status': 'error'
            }), 400
        
        # Dùng RAG chain trực tiếp (nhanh hơn agent rất nhiều)
        result = fast_rag_chain.invoke({"input": user_message})
        bot_reply = result.get("answer", "Xin lỗi, tôi không thể trả lời câu hỏi này.")
        
        # Lưu history (tùy chọn, có thể bỏ để tiết kiệm memory)
        if session_id not in chat_sessions:
            chat_sessions[session_id] = []
        history = chat_sessions[session_id]
        history.append({"role": "user", "content": user_message})
        history.append({"role": "assistant", "content": bot_reply})
        # Giới hạn history để tránh quá dài (chỉ giữ 10 tin nhắn gần nhất)
        if len(history) > 20:
            chat_sessions[session_id] = history[-20:]
        
        return jsonify({
            'response': bot_reply,
            'session_id': session_id,
            'status': 'success'
        })
    
    except Exception as e:
        print(f"Error in chat_fast: {str(e)}")
        return jsonify({
            'error': f'Internal server error: {str(e)}',
            'status': 'error'
        }), 500

@app.route('/api/chat/clear', methods=['POST'])
def clear_chat():
    try:
        data = request.get_json()
        session_id = data.get('session_id', 'default')
        
        if session_id in chat_sessions:
            del chat_sessions[session_id]
        
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
    return jsonify({
        'status': 'healthy',
        'model': 'gemini-2.5-flash',
        'tools': ['chillstay_knowledge_base', 'tavily_search']
    })

if __name__ == '__main__':
    print("Starting ChillStay Chatbot API...")
    print("Vector store initialized with documents")
    print("Agent ready with tools: chillstay_knowledge_base, tavily_search")
    print("Server running on http://0.0.0.0:5000")
    # debug=False cho môi trường ngoài dev
    app.run(host='0.0.0.0', port=5000, debug=False)