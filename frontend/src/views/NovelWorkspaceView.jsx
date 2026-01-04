import { useState, useEffect } from 'react';
import { useParams, Link, Outlet } from 'react-router-dom';
import axios from 'axios';

const NovelWorkspaceView = () => {
  const { id } = useParams();
  const [novel, setNovel] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sidebarOpen, setSidebarOpen] = useState(true);

  useEffect(() => {
    fetchNovelDetails();
  }, [id]);

  const fetchNovelDetails = async () => {
    try {
      const novelResponse = await axios.get(`/api/v1/novels/${id}`);
      setNovel(novelResponse.data);
      
      const chaptersResponse = await axios.get(`/api/v1/novels/${id}/chapters`);
      setChapters(chaptersResponse.data);
      
      setLoading(false);
    } catch (error) {
      console.error('获取小说详情失败:', error);
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  if (!novel) {
    return <div className="error">小说不存在</div>;
  }

  return (
    <div className="novel-workspace">
      <div className={`sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
        <div className="novel-info">
          <input
            type="text"
            value={novel.title}
            onChange={(e) => setNovel({ ...novel, title: e.target.value })}
            onBlur={() => {
              // 保存标题更改
              axios.put(`/api/v1/novels/${id}`, novel);
            }}
            className="novel-title-input"
          />
          <div className="novel-stats">
            <span>章节数: {chapters.length}</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <Link to={`/novel/${id}`} className="nav-link active">
            📖 章节列表
          </Link>
          <Link to={`/novel/${id}/characters`} className="nav-link">
            👤 角色
          </Link>
          <Link to={`/novel/${id}/writing-style`} className="nav-link">
            ⚙️ 风格配置
          </Link>
        </nav>

        <div className="chapter-list">
          <h4>章节</h4>
          <ul>
            {chapters
              .sort((a, b) => a.chapterNumber - b.chapterNumber)
              .map((chapter) => (
                <li key={chapter.id} className="chapter-item">
                  <Link to={`/novel/${id}/chapter/${chapter.chapterNumber}`}>
                    第{chapter.chapterNumber}章 - {chapter.title}
                  </Link>
                </li>
              ))}
          </ul>
        </div>
      </div>

      <button className="sidebar-toggle" onClick={() => setSidebarOpen(!sidebarOpen)}>
        {sidebarOpen ? '◀' : '▶'}
      </button>

      <main className="workspace-main">
        <Outlet />
      </main>
    </div>
  );
};

export default NovelWorkspaceView;