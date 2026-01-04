import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const WritingStyleView = () => {
  const { id: novelId } = useParams();
  const [writingStyle, setWritingStyle] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchWritingStyle();
  }, [novelId]);

  const fetchWritingStyle = async () => {
    try {
      const response = await axios.get(`/api/v1/novels/${novelId}/writing-style`);
      setWritingStyle(response.data);
      setLoading(false);
    } catch (error) {
      console.error('获取写作风格失败:', error);
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  if (!writingStyle) {
    return (
      <div className="writing-style-view">
        <h3>风格配置</h3>
        <div className="no-data">暂无风格信息</div>
        <p className="info">系统将基于前3章内容自动学习写作风格</p>
      </div>
    );
  }

  // 解析JSON字段
  const frequentVerbs = writingStyle.frequentVerbs ? JSON.parse(writingStyle.frequentVerbs) : [];
  const bannedWords = writingStyle.bannedWords ? JSON.parse(writingStyle.bannedWords) : [];
  const descriptionProfile = writingStyle.descriptionProfile ? JSON.parse(writingStyle.descriptionProfile) : {};

  return (
    <div className="writing-style-view">
      <h3>风格配置</h3>
      <div className="style-card">
        <h4>系统提取的风格</h4>
        <div className="style-info">
          <div className="style-item">
            <label>平均句长：</label>
            <span>{writingStyle.avgSentenceLength || '未设置'} 字</span>
          </div>
          <div className="style-item">
            <label>高频动词：</label>
            <span>{frequentVerbs.length > 0 ? frequentVerbs.join('、') : '无'}</span>
          </div>
          <div className="style-item">
            <label>禁用词：</label>
            <span>{bannedWords.length > 0 ? bannedWords.join('、') : '无'}</span>
          </div>
          <div className="style-item">
            <label>叙事视角：</label>
            <span>{writingStyle.emotionalDistance || '未设置'}</span>
          </div>
          <div className="style-item">
            <label>立场：</label>
            <span>{writingStyle.moralStance || '未设置'}</span>
          </div>
          <div className="style-item">
            <label>幽默风格：</label>
            <span>{writingStyle.humorStyle || '未设置'}</span>
          </div>
          <div className="style-item">
            <label>描写风格：</label>
            <span>
              {descriptionProfile.detailLevel || '未设置'}，{descriptionProfile.focus || '未设置'}
            </span>
          </div>
        </div>
        <p className="info">此风格基于前3章自动学习</p>
      </div>
    </div>
  );
};

export default WritingStyleView;