import requests
import json

def test_health_endpoint():
    """Test health endpoint và hiển thị thông tin chi tiết"""
    
    try:
        print("=" * 70)
        print("KIỂM TRA HEALTH ENDPOINT - CHILLSTAY CHATBOT")
        print("=" * 70)
        
        response = requests.get('http://127.0.0.1:5000/api/health', timeout=10)
        
        if response.status_code == 200:
            data = response.json()
            
            print("\n✅ SERVER STATUS:", data.get('status', 'unknown').upper())
            print("\n" + "─" * 70)
            
            # Model info
            print("\n📊 MODEL INFORMATION:")
            model_info = data.get('model', {})
            print(f"  • LLM: {model_info.get('llm')}")
            print(f"  • Temperature: {model_info.get('temperature')}")
            
            # RAG System
            rag = data.get('rag_system', {})
            
            print("\n📚 DOCUMENTS:")
            docs = rag.get('documents', {})
            print(f"  • Total documents loaded: {docs.get('total_documents_loaded')}")
            print(f"  • Total characters: {docs.get('total_characters'):,}")
            if docs.get('document_sources'):
                print(f"  • Sources:")
                for src in docs.get('document_sources', []):
                    print(f"    - {src}")
            
            print("\n✂️  CHUNKING:")
            chunks = rag.get('chunking', {})
            print(f"  • Total chunks: {chunks.get('total_chunks')}")
            print(f"  • Chunk size: {chunks.get('chunk_size')}")
            print(f"  • Chunk overlap: {chunks.get('chunk_overlap')}")
            print(f"  • Average chunk length: {chunks.get('avg_chunk_length')}")
            print(f"  • Min chunk length: {chunks.get('min_chunk_length')}")
            print(f"  • Max chunk length: {chunks.get('max_chunk_length')}")
            
            print("\n🔢 VECTOR STORE:")
            vectors = rag.get('vector_store', {})
            print(f"  • Vector count: {vectors.get('vector_count')}")
            print(f"  • Embedding model: {vectors.get('embedding_model')}")
            print(f"  • Embedding dimension: {vectors.get('embedding_dimension')}")
            print(f"  • Collection name: {vectors.get('collection_name')}")
            print(f"  • Device: {vectors.get('device')}")
            
            print("\n🔍 RETRIEVAL:")
            retrieval = rag.get('retrieval', {})
            print(f"  • Top-K chunks: {retrieval.get('retrieval_k')}")
            print(f"  • Similarity metric: {retrieval.get('similarity_metric')}")
            
            print("\n🛠️  TOOLS:")
            tools = data.get('tools', {}).get('available_tools', [])
            for tool in tools:
                status_icon = "✅" if tool.get('status') == 'active' else "❌"
                print(f"  {status_icon} {tool.get('name')} ({tool.get('type')})")
            
            print("\n🔥 FIREBASE:")
            firebase_status = "✅ Connected" if data.get('firebase_connected') else "❌ Not Connected"
            print(f"  • Status: {firebase_status}")
            
            print("\n🌐 SERVER:")
            server = data.get('server', {})
            print(f"  • Host: {server.get('host')}")
            print(f"  • Port: {server.get('port')}")
            
            print("\n" + "=" * 70)
            print("✅ HEALTH CHECK COMPLETED SUCCESSFULLY")
            print("=" * 70)
            
            # Lưu kết quả ra file
            with open('health_check_result.json', 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print("\n💾 Chi tiết đã được lưu vào: health_check_result.json")
            
        else:
            print(f"\n❌ ERROR: Server returned status code {response.status_code}")
            print(f"Response: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("\n❌ ERROR: Không thể kết nối đến server!")
        print("Đảm bảo server đang chạy tại http://127.0.0.1:5000")
    except requests.exceptions.Timeout:
        print("\n❌ ERROR: Request timeout!")
    except Exception as e:
        print(f"\n❌ ERROR: {str(e)}")

if __name__ == '__main__':
    test_health_endpoint()