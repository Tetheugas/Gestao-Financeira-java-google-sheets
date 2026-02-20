// Main App component for Gestão Financeira Pessoal
// Requirements: 5.1, 6.1

import { useState, useEffect } from 'react';
import ExpenseList from './components/ExpenseList';
import ExpenseForm from './components/ExpenseForm';
import SpreadsheetConfigModal from './components/SpreadsheetConfigModal';
import {
  getAuthStatus,
  getFilters,
  createFilter,
  renameFilter,
  deleteFilter
} from './services/expenseAPI';

function App() {
  const [aba, setAba] = useState('CartaoNubank');
  const [mes, setMes] = useState('Fevereiro');
  const [filters, setFilters] = useState<string[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [showConfigModal, setShowConfigModal] = useState(false);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const isAuth = await getAuthStatus();
    setShowAuthModal(!isAuth);
    if (isAuth) {
      fetchFilters();
    }
  };

  const fetchFilters = async () => {
    try {
      const data = await getFilters();
      if (data && data.length > 0) {
        setFilters(data);
        // If current aba is not in the list, switch to the first one
        if (!data.includes(aba)) {
          setAba(data[0]);
        }
      }
    } catch (error) {
      console.error("Failed to fetch filters", error);
    }
  };

  const handleLogin = () => {
    try {
      const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
      // Remove /api suffix if present to get base URL
      const baseUrl = apiUrl.endsWith('/api') ? apiUrl.slice(0, -4) : apiUrl;
      window.location.href = `${baseUrl}/oauth2/authorization/google`;
    } catch (error) {
      console.error("Login failed", error);
      alert("Falha ao iniciar login com Google.");
    }
  };

  const handleExpenseAdded = () => {
    // Trigger ExpenseList refresh by updating key
    setRefreshKey(prev => prev + 1);
  };

  const handleAddFilter = async () => {
    const name = window.prompt("Nome da nova aba (filtro):");
    if (name && name.trim()) {
      try {
        await createFilter(name.trim());
        await fetchFilters();
        setAba(name.trim());
      } catch (error) {
        alert("Erro ao criar filtro: " + (error instanceof Error ? error.message : "Erro desconhecido"));
      }
    }
  };

  const handleRenameFilter = async () => {
    const newName = window.prompt("Novo nome para o filtro:", aba);
    if (newName && newName.trim() && newName !== aba) {
      if (confirm(`Tem certeza que deseja renomear "${aba}" para "${newName.trim()}"?`)) {
        try {
          await renameFilter(aba, newName.trim());
          const oldName = aba;
          const updatedName = newName.trim();

          // Update local state directly to reflect change immediately if fetch fails or is slow
          setFilters(prev => prev.map(f => f === oldName ? updatedName : f));
          setAba(updatedName);

          // Also fetch from server to be sure
          fetchFilters();
        } catch (error) {
          alert("Erro ao renomear filtro: " + (error instanceof Error ? error.message : "Erro desconhecido"));
        }
      }
    }
  };

  const handleDeleteFilter = async () => {
    if (filters.length <= 1) {
      alert("Não é possível apagar o único filtro existente.");
      return;
    }

    if (confirm(`Tem certeza que deseja APAGAR o filtro "${aba}" e TODOS os seus dados? Esta ação não pode ser desfeita.`)) {
      try {
        await deleteFilter(aba);

        // Find another filter to switch to
        const remainingFilters = filters.filter(f => f !== aba);
        if (remainingFilters.length > 0) {
          setAba(remainingFilters[0]);
        }

        // Refresh filters
        fetchFilters();
      } catch (error) {
        alert("Erro ao apagar filtro: " + (error instanceof Error ? error.message : "Erro desconhecido"));
      }
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <header className="mb-10 text-center relative">
          <div className="absolute right-0 top-0">
            <button
              onClick={() => setShowConfigModal(true)}
              className="p-2 text-gray-500 hover:text-gray-700 focus:outline-none"
              title="Configurações da Planilha"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
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
                  <div className="flex justify-between items-center mb-1">
                    <label htmlFor="aba" className="block text-sm font-medium text-gray-700">
                      Conta / Cartão
                    </label>
                    <div className="flex space-x-2">
                      <button
                        onClick={handleRenameFilter}
                        className="text-xs text-gray-500 hover:text-indigo-600 font-medium focus:outline-none flex items-center"
                        title="Renomear aba atual"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                        </svg>
                        Editar
                      </button>
                      <button
                        onClick={handleDeleteFilter}
                        className="text-xs text-gray-500 hover:text-red-600 font-medium focus:outline-none flex items-center"
                        title="Apagar aba atual"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                        Apagar
                      </button>
                    </div>
                  </div>
                  <div className="relative mb-2">
                    <select
                      id="aba"
                      value={aba}
                      onChange={(e) => setAba(e.target.value)}
                      className="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 px-3 bg-gray-50 border"
                    >
                      {filters.length > 0 ? (
                        filters.map(filter => (
                          <option key={filter} value={filter}>{filter}</option>
                        ))
                      ) : (
                        <option value="CartaoNubank">Carregando...</option>
                      )}
                    </select>
                  </div>
                  <div className="text-right">
                    <button
                        onClick={handleAddFilter}
                        className="text-xs text-indigo-600 hover:text-indigo-800 font-medium focus:outline-none"
                      >
                        + Nova Aba
                    </button>
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

      {/* Spreadsheet Config Modal */}
      <SpreadsheetConfigModal
        isOpen={showConfigModal}
        onClose={() => setShowConfigModal(false)}
      />

      {/* Auth Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-75 overflow-y-auto h-full w-full flex items-center justify-center z-50">
          <div className="bg-white p-8 rounded-xl shadow-2xl text-center max-w-md mx-auto border border-gray-200">
            <div className="mb-6">
              <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-blue-100 mb-4">
                <svg className="h-8 w-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Conectar Conta Google</h2>
              <p className="text-gray-600">
                Para utilizar o sistema, é necessário conectar sua conta Google para acessar as planilhas de gastos.
              </p>
            </div>
            <button
              onClick={handleLogin}
              className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200"
            >
              Conectar com Google
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
