import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import CategoryPage from "./pages/CategoryPage";
import PublisherPage from "./pages/PublisherPage";
import AuthorPage from "./pages/AuthorPage";
import BookPage from "./pages/BookPage";
import BorrowPage from "./pages/BorrowPage";
import AuthPage from "./pages/AuthPage";
import UserPage from "./pages/UserPage";
import FinePage from "./pages/FinePage";
import HomePage from "./pages/HomePage";
import DashboardPage from "./pages/DashboardPage";
import AppShell from "./layouts/AppShell";
import RequireAuth from "./components/RequireAuth";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<AuthPage mode="login" />} />
        <Route path="/register" element={<AuthPage mode="register" />} />
        <Route
          path="/*"
          element={
            <RequireAuth>
              <AppShell>
                <Routes>
                  <Route path="/" element={<HomePage />} />
                  <Route
                    path="/dashboard"
                    element={
                      <RequireAuth allowedRoles={["ADMIN", "LIBRARIAN"]}>
                        <DashboardPage />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="/categories"
                    element={
                      <RequireAuth allowedRoles={["ADMIN", "LIBRARIAN"]}>
                        <CategoryPage />
                      </RequireAuth>
                    }
                  />
                  <Route path="/books" element={<BookPage />} />
                  <Route path="/borrows" element={<BorrowPage />} />
                  <Route
                    path="/users"
                    element={
                      <RequireAuth allowedRoles={["ADMIN"]}>
                        <UserPage />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="/fines"
                    element={
                      <RequireAuth allowedRoles={["ADMIN", "LIBRARIAN", "USER"]}>
                        <FinePage />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="/authors"
                    element={
                      <RequireAuth allowedRoles={["ADMIN", "LIBRARIAN"]}>
                        <AuthorPage />
                      </RequireAuth>
                    }
                  />
                  <Route
                    path="/publishers"
                    element={
                      <RequireAuth allowedRoles={["ADMIN", "LIBRARIAN"]}>
                        <PublisherPage />
                      </RequireAuth>
                    }
                  />
                </Routes>
              </AppShell>
            </RequireAuth>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
