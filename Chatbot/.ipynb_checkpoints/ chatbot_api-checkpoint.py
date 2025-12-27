from flask import Flask, request, jsonify
from flask_cors import CORS
from langchain_core.prompts import ChatPromptTemplate
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_chroma import Chroma
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_tavily import TavilySearch
from langgraph.prebuilt import create_react_agent
from pydantic import BaseModel, Field
from typing import Type, Optional
from langchain.tools import BaseTool
from langchain.callbacks.manager import CallbackManagerForToolRun
import os

app = Flask(__name__)
CORS(app)

# ==================== SETUP CHATBOT ====================

# API Keys (nên lưu trong .env file)
os.environ["GOOGLE_API_KEY"] = "AIzaSyD22V8HmqM7cX3AfIJW5yH-TkSqVY2mmjg"
os.environ["TAVILY_API_KEY"] = ""

# Initialize LLM
llm = ChatGoogleGenerativeAI(
    model="gemini-2.5-flash",
    temperature=0,
    max_tokens=None,
)

# Load documents
documents = []
for filename in os.listdir('.'):
    if filename.endswith('.txt'):
        with open(filename, 'r', encoding='utf-8') as file:
            content = file.read()
            documents.append(
                Document(
                    page_content=content,
                    metadata={'source': filename}
                )
            )

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
    max_results=2,
    topic="general",
)

# Create agent
agent = create_react_agent(llm, [chillstay_tool, search_web_tool])

# ==================== SESSION MANAGEMENT ====================

# Store chat histories in memory (trong production nên dùng Redis/Database)
chat_sessions = {}

# ==================== API ENDPOINTS ====================

@app.route('/api/chat', methods=['POST'])
def chat():
    """
    API endpoint để chat với bot
    Request body: {
        "message": "user message",
        "session_id": "unique_session_id" (optional)
    }
    """
    try:
        data = request.get_json()
        user_message = data.get('message', '').strip()
        session_id = data.get('session_id', 'default')
        
        if not user_message:
            return jsonify({
                'error': 'Message cannot be empty',
                'status': 'error'
            }), 400
        
        # Lấy hoặc tạo history cho session
        if session_id not in chat_sessions:
            chat_sessions[session_id] = []
        
        history = chat_sessions[session_id]
        
        # Thêm message của user vào history
        messages = history.copy()
        messages.append({"role": "user", "content": user_message})
        
        # Gọi agent
        result = agent.invoke({"messages": messages})
        bot_reply = result['messages'][-1].content
        
        # Lưu vào history
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

@app.route('/api/chat/clear', methods=['POST'])
def clear_chat():
    """
    Xóa lịch sử chat của một session
    Request body: {
        "session_id": "unique_session_id"
    }
    """
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
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'model': 'gemini-2.5-flash',
        'tools': ['chillstay_knowledge_base', 'tavily_search']
    })

if __name__ == '__main__':
    print("🚀 Starting ChillStay Chatbot API...")
    print("📚 Vector store initialized with documents")
    print("🤖 Agent ready with tools: chillstay_knowledge_base, tavily_search")
    print("🌐 Server running on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=True)