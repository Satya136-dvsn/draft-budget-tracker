import { Routes, Route } from 'react-router-dom'
import HomePage from '../pages/HomePage'
import SignIn from '../pages/SignIn'
import SignUp from '../pages/SignUp'
import Dashboard from '../pages/Dashboard'
import Transactions from '../pages/Transactions'
import Categories from '../pages/Categories'
import Budgets from '../pages/Budgets'
import SavingsGoals from '../pages/SavingsGoals'
import DesignSystemDemo from '../pages/DesignSystemDemo'
import ProtectedRoute from '../components/ProtectedRoute'
import DashboardLayout from '../components/layout/DashboardLayout'
import Analytics from '../pages/Analytics'
import Reports from '../pages/Reports'
import Settings from '../pages/Settings'
import Community from '../pages/Community'
import DiscussionDetail from '../pages/DiscussionDetail'
import AdminDashboard from '../pages/AdminDashboard'
import BankingPage from '../pages/BankingPage'
import BillsPage from '../pages/BillsPage'
import InvestmentsPage from '../pages/InvestmentsPage'
import AIChat from '../pages/AIChat'

import Notifications from '../pages/Notifications'

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<SignIn />} />
      <Route path="/register" element={<SignUp />} />
      <Route path="/design-demo" element={<DesignSystemDemo />} />
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/categories" element={<Categories />} />
        <Route path="/budgets" element={<Budgets />} />
        <Route path="/goals" element={<SavingsGoals />} />
        <Route path="/analytics" element={<Analytics />} />
        <Route path="/reports" element={<Reports />} />
        <Route path="/settings" element={<Settings />} />
        <Route path="/community" element={<Community />} />
        <Route path="/community/:id" element={<DiscussionDetail />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/banking" element={<BankingPage />} />
        <Route path="/bills" element={<BillsPage />} />
        <Route path="/investments" element={<InvestmentsPage />} />
        <Route path="/ai-chat" element={<AIChat />} />
        <Route path="/notifications" element={<Notifications />} />
      </Route>
    </Routes>
  )
}

export default AppRoutes
