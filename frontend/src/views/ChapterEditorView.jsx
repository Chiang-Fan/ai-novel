import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const ChapterEditorView = () => {
  const { id: novelId, chapterNumber: paramChapterNumber } = useParams();
  const [chapter, setChapter] = useState({ content: '', title: '' });
  const [loading, setLoading] = useState(false);
  const [showDirectionModal, setShowDirectionModal] = useState(false);
  const [direction, setDirection] = useState('');
  const [bannedElements, setBannedElements] = useState('');
  const [mood, setMood] = useState('平静');
  const [overrideHighStakes, setOverrideHighStakes] = useState(false);
  const [characters, setCharacters] = useState([]);
  const [plotHooks, setPlotHooks] = useState([]);

  // 从参数获取章节号，如果未指定则默认为1
  const chapterNumber = paramChapterNumber ? parseInt(paramChapterNumber) : 1;

  useEffect(() => {
    fetchChapter();
    fetchCharacters();
    fetchPlotHooks();
  }, [novelId, chapterNumber]);

  const fetchChapter = async () => {
    try {
      const response = await axios.get(`/api/v1/novels/${novelId}/chapters/${chapterNumber}`);
      if (response.data) {
        setChapter(response.data);
      } else {
        // 如果章节不存在，初始化为空
        setChapter({ content: '', title: `第${chapterNumber}章` });
      }
    } catch (error) {
      console.error('获取章节失败:', error);
      setChapter({ content: '', title: `第${chapterNumber}章` });
    }
  };

  const fetchCharacters = async () => {
    try {
      const response = await axios.get(`/api/v1/novels/${novelId}/characters`);
      setCharacters(response.data);
    } catch (error) {
      console.error('获取角色列表失败:', error);
    }
  };

  const fetchPlotHooks = async () => {
    try {
      const response = await axios.get(`/api/v1/novels/${novelId}/plot-hooks`);
      // 过滤出待触发的伏笔
      const pendingHooks = response.data.filter(hook => hook.status === 'PENDING');
      setPlotHooks(pendingHooks);
    } catch (error) {
      console.error('获取伏笔列表失败:', error);
    }
  };

  const handleSaveChapter = async () => {
    // 暂时保存到状态，实际实现需要后端支持
    alert('章节已保存');
  };

  const handleGenerateChapter = async () => {
    setLoading(true);
    try {
      const response = await axios.post(`/api/v1/novels/${novelId}/chapters`, null, {
        params: { chapterNumber }
      });
      setChapter(response.data.chapter);
    } catch (error) {
      console.error('生成章节失败:', error);
      alert('生成章节失败: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateChapterWithDirection = async () => {
    if (!direction.trim()) {
      alert('请输入续写方向');
      return;
    }

    setLoading(true);
    try {
      const requestBody = {
        chapterNumber,
        direction,
        bannedElements: bannedElements ? bannedElements.split(',').map(s => s.trim()) : [],
        mood,
        overrideHighStakes
      };

      const response = await axios.post(`/api/v1/novels/${novelId}/chapters/directed/${chapterNumber}`, requestBody);
      setChapter(response.data.chapter);
      setShowDirectionModal(false);
    } catch (error) {
      console.error('生成章节失败:', error);
      alert('生成章节失败: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const openDirectionModal = () => {
    setShowDirectionModal(true);
  };

  const closeDirectionModal = () => {
    setShowDirectionModal(false);
    // 重置表单
    setDirection('');
    setBannedElements('');
    setMood('平静');
    setOverrideHighStakes(false);
  };

  // 计算字数
  const wordCount = chapter.content ? chapter.content.length : 0;

  return (
    <div className="chapter-editor-view">
      <div className="editor-header">
        <div className="editor-title">
          <input
            type="text"
            value={chapter.title || `第${chapterNumber}章`}
            onChange={(e) => setChapter({ ...chapter, title: e.target.value })}
            className="chapter-title-input"
          />
        </div>
        <div className="editor-actions">
          <button onClick={handleGenerateChapter} disabled={loading} className="btn-secondary">
            {loading ? '生成中...' : 'AI续写下一章'}
          </button>
          <button onClick={openDirectionModal} disabled={loading} className="btn-primary">
            AI续写(带方向)...
          </button>
          <button onClick={handleSaveChapter} className="btn-success">
            保存
          </button>
        </div>
      </div>

      {/* 伏笔提示条 */}
      {plotHooks.length > 0 && (
        <div className="plot-hook-suggestion-bar">
          <strong>💡 本章可展开伏笔：</strong>
          {plotHooks.map((hook, index) => (
            <span key={hook.id} className="plot-hook-suggestion">
              <strong>“{hook.description}”</strong>
              {index < plotHooks.length - 1 && '，'}
            </span>
          ))}
        </div>
      )}

      <div className="editor-content">
        <textarea
          value={chapter.content}
          onChange={(e) => setChapter({ ...chapter, content: e.target.value })}
          className="chapter-content-textarea"
          placeholder="在这里输入章节内容..."
        />
      </div>

      <div className="editor-footer">
        <div className="word-count">字数: {wordCount}</div>
        <div className="rhythm-info">当前为铺垫章节</div> {/* 这里可以接入实际的节奏逻辑 */}
      </div>

      {/* 续写方向模态框 */}
      {showDirectionModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>续写方向</h3>
            
            <div className="modal-form">
              <div className="form-group">
                <label>目标章节数:</label>
                <input
                  type="number"
                  value={chapterNumber}
                  disabled
                  className="readonly-input"
                />
              </div>
              
              <div className="form-group">
                <label>续写方向 (必填):</label>
                <textarea
                  value={direction}
                  onChange={(e) => setDirection(e.target.value)}
                  placeholder="用几句话告诉 AI 你希望这一章发生什么。越具体，结果越贴近你的想象。例如：'主角在雨夜发现信件，情绪崩溃但强忍泪水'"
                  rows={4}
                />
              </div>
              
              <div className="form-group">
                <label>禁止事项 (可选):</label>
                <input
                  type="text"
                  value={bannedElements}
                  onChange={(e) => setBannedElements(e.target.value)}
                  placeholder="逗号分隔，例如：战斗,回忆童年"
                />
              </div>
              
              <div className="form-group">
                <label>情绪基调 (可选):</label>
                <select value={mood} onChange={(e) => setMood(e.target.value)}>
                  <option value="平静">平静</option>
                  <option value="紧张">紧张</option>
                  <option value="忧伤">忧伤</option>
                  <option value="悬疑">悬疑</option>
                  <option value="荒诞">荒诞</option>
                </select>
              </div>
              
              <div className="form-group checkbox-group">
                <label>
                  <input
                    type="checkbox"
                    checked={overrideHighStakes}
                    onChange={(e) => setOverrideHighStakes(e.target.checked)}
                  />
                  是否高重要场景
                </label>
                <small>默认由系统建议，可手动覆盖</small>
              </div>
            </div>
            
            <div className="modal-actions">
              <button onClick={handleGenerateChapterWithDirection} disabled={loading || !direction}>
                {loading ? '生成中...' : '生成章节'}
              </button>
              <button onClick={closeDirectionModal} className="btn-secondary">
                取消
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChapterEditorView;