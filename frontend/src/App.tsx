// Main App component for Gestão Financeira Pessoal
// Requirements: 5.1, 6.1

import { useState } from 'react';
import ExpenseList from './components/ExpenseList';
import ExpenseForm from './components/ExpenseForm';

function App() {
  const [aba, setAba] = useState('CartaoNubank');
  const [mes, setMes] = useState('Fevereiro');
  const [refreshKey, setRefreshKey] = useState(0);

  const handleExpenseAdded = () => {
    // Trigger ExpenseList refresh by updating key
    setRefreshKey(prev => prev + 1);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">
          Gestão Financeira Pessoal
        </h1>

        {/* Selectors for aba and mes */}
        <div className="bg-white p-6 rounded-lg shadow border border-gray-200 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label htmlFor="aba" className="block text-sm font-medium text-gray-700 mb-2">
                Aba
              </label>
              <select
                id="aba"
                value={aba}
                onChange={(e) => setAba(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="CartaoNubank">Cartão Nubank</option>
                <option value="CartaoInter">Cartão Inter</option>
                <option value="Dinheiro">Dinheiro</option>
              </select>
            </div>

            <div>
              <label htmlFor="mes" className="block text-sm font-medium text-gray-700 mb-2">
                Mês
              </label>
              <select
                id="mes"
                value={mes}
                onChange={(e) => setMes(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="Janeiro">Janeiro</option>
                <option value="Fevereiro">Fevereiro</option>
                <option value="Marco">Março</option>
                <option value="Abril">Abril</option>
                <option value="Maio">Maio</option>
                <option value="Junho">Junho</option>
                <option value="Julho">Julho</option>
                <option value="Agosto">Agosto</option>
                <option value="Setembro">Setembro</option>
                <option value="Outubro">Outubro</option>
                <option value="Novembro">Novembro</option>
                <option value="Dezembro">Dezembro</option>
              </select>
            </div>
          </div>
        </div>

        {/* ExpenseForm */}
        <div className="mb-6">
          <ExpenseForm
            aba={aba}
            mes={mes}
            onExpenseAdded={handleExpenseAdded}
          />
        </div>

        {/* ExpenseList */}
        <div>
          <h2 className="text-xl font-semibold text-gray-800 mb-4">
            Gastos de {mes} - {aba}
          </h2>
          <ExpenseList
            key={refreshKey}
            mes={mes}
            aba={aba}
          />
        </div>
      </div>
    </div>
  );
}

export default App;
