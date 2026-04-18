"""
诊断脚本
"""
import sys
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from database.database import SessionLocal
from agents.tools.database_query import DatabaseQueryTool

db = SessionLocal()

try:
    db_tool = DatabaseQueryTool(db)
    
    print("Testing get_user_profile(1)...")
    profile = db_tool.get_user_profile(1)
    
    if profile:
        print("SUCCESS! Profile found:")
        print(profile)
    else:
        print("FAILED: Profile is None")
        
except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()
finally:
    db.close()

