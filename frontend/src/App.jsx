import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import DashboardView from './views/DashboardView';
import NovelWorkspaceView from './views/NovelWorkspaceView';
import ChapterEditorView from './views/ChapterEditorView';
import CharactersView from './views/CharactersView';
import WritingStyleView from './views/WritingStyleView';
import PlotHooksView from './views/PlotHooksView';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <header className="app-header">
          <h1>NovelCraft AI 写作助手</h1>
        </header>
        <main>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardView />} />
            <Route path="/novel/:id" element={<NovelWorkspaceView />}>
              <Route index element={<ChapterEditorView />} />
              <Route path="chapter/:chapterNumber" element={<ChapterEditorView />} />
              <Route path="characters" element={<CharactersView />} />
              <Route path="writing-style" element={<WritingStyleView />} />
              <Route path="plot-hooks" element={<PlotHooksView />} />
            </Route>
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;