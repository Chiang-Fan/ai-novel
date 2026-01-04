import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const PlotHooksView = () => {
  const { id: novelId } = useParams();
  const [plotHooks, setPlotHooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newPlotHook, setNewPlotHook] = useState({
    description: '',
    minChapter: 1,
    maxChapter: 10,
    contextSnippet: ''
  });

  useEffect(() => {
    fetchPlotHooks();
  }, [novelId]);

  const fetchPlotHooks = async () => {
    try {
      const response = await axios.get(`/api/v1/novels/${novelId}/plot-hooks`);
      setPlotHooks(response.data);
      setLoading(false);
    } catch (error) {
      console.error('获取伏笔列表失败:', error);
      setLoading(false);
    }
  };

  const handleCreatePlotHook = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`/api/v1/novels/${novelId}/plot-hooks`, newPlotHook);
      setNewPlotHook({
        description: '',
        minChapter: 1,
        maxChapter: 10,
        contextSnippet: ''
      });
      setShowModal(false);
      fetchPlotHooks(); // 重新获取列表
    } catch (error) {
      console.error('创建伏笔失败:', error);
    }
  };

  const updatePlotHookStatus = async (id, status) => {
    try {
      await axios.patch(`/api/v1/novels/${novelId}/plot-hooks/${id}/status`, status);
      fetchPlotHooks(); // 重新获取列表
    } catch (error) {
      console.error('更新伏笔状态失败:', error);
    }
  };

  const deletePlotHook = async (id) => {
    if (window.confirm('确定要删除这个伏笔吗？')) {
      try {
        await axios.delete(`/api/v1/novels/${novelId}/plot-hooks/${id}`);
        fetchPlotHooks(); // 重新获取列表
      } catch (error) {
        console.error('删除伏笔失败:', error);
      }
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div className="plot-hooks-view">
      <div className="plot-hooks-header">
        <h3>伏笔管理</h3>
        <button onClick={() => setShowModal(true)} className="btn-primary">
          + 添加伏笔
        </button>
      </div>

      <div className="plot-hooks-table-container">
        <table className="plot-hooks-table">
          <thead>
            <tr>
              <th>伏笔描述</th>
              <th>状态</th>
              <th>可展开区间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {plotHooks.map((hook) => (
              <tr key={hook.id}>
                <td>{hook.description}</td>
                <td>
                  <span className={`status-${hook.status.toLowerCase()}`}>
                    {hook.status === 'PENDING' ? '待触发' : hook.status === 'TRIGGERED' ? '已触发' : '已回收'}
                  </span>
                </td>
                <td>第 {hook.minChapter} - {hook.maxChapter} 章</td>
                <td>
                  {hook.status === 'PENDING' && (
                    <button 
                      onClick={() => updatePlotHookStatus(hook.id, 'RESOLVED')}
                      className="btn-small btn-success"
                    >
                      标记已回收
                    </button>
                  )}
                  <button 
                    onClick={() => deletePlotHook(hook.id)}
                    className="btn-small btn-danger"
                    style={{ marginLeft: '5px' }}
                  >
                    删除
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {plotHooks.length === 0 && (
        <div className="no-data">暂无伏笔信息</div>
      )}

      {/* 添加伏笔模态框 */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>添加伏笔</h3>
            <form onSubmit={handleCreatePlotHook}>
              <div className="form-group">
                <label htmlFor="description">伏笔描述 *</label>
                <textarea
                  id="description"
                  value={newPlotHook.description}
                  onChange={(e) => setNewPlotHook({ ...newPlotHook, description: e.target.value })}
                  required
                  rows={3}
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="minChapter">最小章节</label>
                  <input
                    id="minChapter"
                    type="number"
                    min="1"
                    value={newPlotHook.minChapter}
                    onChange={(e) => setNewPlotHook({ ...newPlotHook, minChapter: parseInt(e.target.value) })}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="maxChapter">最大章节</label>
                  <input
                    id="maxChapter"
                    type="number"
                    min="1"
                    value={newPlotHook.maxChapter}
                    onChange={(e) => setNewPlotHook({ ...newPlotHook, maxChapter: parseInt(e.target.value) })}
                  />
                </div>
              </div>
              <div className="form-group">
                <label htmlFor="contextSnippet">上下文片段</label>
                <textarea
                  id="contextSnippet"
                  value={newPlotHook.contextSnippet}
                  onChange={(e) => setNewPlotHook({ ...newPlotHook, contextSnippet: e.target.value })}
                  rows={2}
                />
              </div>
              <div className="modal-actions">
                <button type="submit" className="btn-primary">
                  添加
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

export default PlotHooksView;