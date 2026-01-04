import { useState } from 'react';
import axios from 'axios';
import './ChapterEditor.css';

const ChapterEditor = () => {
  const [novelId, setNovelId] = useState('');
  const [chapterNumber, setChapterNumber] = useState('');
  const [chapterContent, setChapterContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [showDirectionModal, setShowDirectionModal] = useState(false);
  
  // 续写方向相关状态
  const [direction, setDirection] = useState('');
  const [bannedElements, setBannedElements] = useState('');
  const [mood, setMood] = useState('平静');
  const [overrideHighStakes, setOverrideHighStakes] = useState(false);

  const handleGenerateChapter = async () => {
    if (!novelId || !chapterNumber) {
      alert('请输入小说ID和章节号');
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post(`/api/v1/novels/${novelId}/chapters`, null, {
        params: { chapterNumber: parseInt(chapterNumber) }
      });
      setChapterContent(response.data.chapter.content);
    } catch (error) {
      console.error('生成章节失败:', error);
      alert('生成章节失败: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateChapterWithDirection = async () => {
    if (!novelId || !chapterNumber) {
      alert('请输入小说ID和章节号');
      return;
    }

    setLoading(true);
    try {
      const requestBody = {
        chapterNumber: parseInt(chapterNumber),
        direction,
        bannedElements: bannedElements ? bannedElements.split(',') : [],
        mood,
        overrideHighStakes
      };

      const response = await axios.post(`/api/v1/novels/${novelId}/chapters/directed`, requestBody, {
        params: { chapterNumber: parseInt(chapterNumber) }
      });
      setChapterContent(response.data.chapter.content);
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

  return (
    <div className="chapter-editor">
      <div className="input-section">
        <div className="input-group">
          <label htmlFor="novelId">小说ID:</label>
          <input
            id="novelId"
            type="text"
            value={novelId}
            onChange={(e) => setNovelId(e.target.value)}
            placeholder="输入小说ID"
          />
        </div>
        
        <div className="input-group">
          <label htmlFor="chapterNumber">章节号:</label>
          <input
            id="chapterNumber"
            type="number"
            value={chapterNumber}
            onChange={(e) => setChapterNumber(e.target.value)}
            placeholder="输入章节号"
          />
        </div>
        
        <div className="button-group">
          <button onClick={handleGenerateChapter} disabled={loading}>
            {loading ? '生成中...' : 'AI续写下一章'}
          </button>
          <button onClick={openDirectionModal} disabled={loading}>
            AI续写(带方向)...
          </button>
        </div>
      </div>

      {chapterContent && (
        <div className="output-section">
          <h3>生成的章节内容</h3>
          <div className="chapter-content">
            <pre>{chapterContent}</pre>
          </div>
        </div>
      )}

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
              <button onClick={closeDirectionModal} className="secondary">
                取消
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChapterEditor;