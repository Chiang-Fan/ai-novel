import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

const DashboardView = () => {
  const [novels, setNovels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newNovel, setNewNovel] = useState({ title: '', outline: '' });

  useEffect(() => {
    fetchNovels();
  }, []);

  const fetchNovels = async () => {
    try {
      const response = await axios.get('/api/v1/novels');
      setNovels(response.data);
      setLoading(false);
    } catch (error) {
      console.error('获取小说列表失败:', error);
      setLoading(false);
    }
  };

  const handleCreateNovel = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/v1/novels', newNovel);
      setNewNovel({ title: '', outline: '' });
      setShowModal(false);
      fetchNovels(); // 重新获取小说列表
    } catch (error) {
      console.error('创建小说失败:', error);
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>小说仪表盘</h2>
        <button onClick={() => setShowModal(true)} className="btn-primary">
          + 新建小说
        </button>
      </div>

      <div className="novel-grid">
        {novels.map((novel) => (
          <div key={novel.id} className="novel-card">
            <h3>
              <Link to={`/novel/${novel.id}`}>{novel.title}</Link>
            </h3>
            <p className="novel-outline">{novel.outline || '暂无大纲'}</p>
            <div className="novel-meta">
              <span>最后编辑: {new Date(novel.updatedAt).toLocaleString()}</span>
              <span>章节数: {novel.chapterCount || 0}</span>
            </div>
          </div>
        ))}
      </div>

      {/* 创建小说模态框 */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>创建新小说</h3>
            <form onSubmit={handleCreateNovel}>
              <div className="form-group">
                <label htmlFor="title">标题 *</label>
                <input
                  id="title"
                  type="text"
                  value={newNovel.title}
                  onChange={(e) => setNewNovel({ ...newNovel, title: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="outline">大纲</label>
                <textarea
                  id="outline"
                  value={newNovel.outline}
                  onChange={(e) => setNewNovel({ ...newNovel, outline: e.target.value })}
                  rows={4}
                />
              </div>
              <div className="modal-actions">
                <button type="submit" className="btn-primary">
                  创建
                </button>
                <button type="button" onClick={() => setShowModal(false)} className="btn-secondary">
                  取消
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardView;