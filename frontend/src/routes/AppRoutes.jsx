import { Routes, Route, Navigate } from 'react-router-dom'
import Login from '../pages/Login'
import Register from '../pages/Register'
import Dashboard from '../pages/Dashboard'
import Transactions from '../pages/Transactions'
import Categories from '../pages/Categories'
import Budgets from '../pages/Budgets'
import SavingsGoals from '../pages/SavingsGoals'
import ProtectedRoute from '../components/ProtectedRoute'
import Layout from '../components/Layout'

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/categories" element={<Categories />} />
        <Route path="/budgets" element={<Budgets />} />
        <Route path="/goals" element={<SavingsGoals />} />
      </Route>
    </Routes>
  )
}

export default AppRoutes
