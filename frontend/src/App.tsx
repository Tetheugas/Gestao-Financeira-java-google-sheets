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
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <header className="mb-10 text-center">
          <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl tracking-tight">
            Gestão Financeira
          </h1>
          <p className="mt-2 text-lg text-gray-600">
            Controle seus gastos de forma simples e eficiente.
          </p>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Sidebar: Selectors and Form */}
          <div className="lg:col-span-1 space-y-6">
            {/* Selectors Card */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
              <h2 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-100 pb-2">
                Filtros
              </h2>
              <div className="space-y-4">
                <div>
                  <label htmlFor="aba" className="block text-sm font-medium text-gray-700 mb-1">
                    Conta / Cartão
                  </label>
                  <div className="relative">
                    <select
                      id="aba"
                      value={aba}
                      onChange={(e) => setAba(e.target.value)}
                      className="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 px-3 bg-gray-50 border"
                    >
                      <option value="CartaoNubank">Cartão Nubank</option>
                      <option value="CartaoInter">Cartão Inter</option>
                      <option value="Dinheiro">Dinheiro</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label htmlFor="mes" className="block text-sm font-medium text-gray-700 mb-1">
                    Mês de Referência
                  </label>
                  <div className="relative">
                    <select
                      id="mes"
                      value={mes}
                      onChange={(e) => setMes(e.target.value)}
                      className="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 px-3 bg-gray-50 border"
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
            </div>

            {/* Expense Form */}
            <ExpenseForm
              aba={aba}
              mes={mes}
              onExpenseAdded={handleExpenseAdded}
            />
          </div>

          {/* Main Content: Expense List */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="px-6 py-5 border-b border-gray-200 bg-gray-50 flex justify-between items-center">
                <h2 className="text-xl font-semibold text-gray-800">
                  Extrato
                </h2>
                <div className="text-sm text-gray-500 bg-white px-3 py-1 rounded-full border border-gray-200 shadow-sm">
                  {mes} • {aba}
                </div>
              </div>
              <div className="p-0">
                <ExpenseList
                  key={refreshKey}
                  mes={mes}
                  aba={aba}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
