// ExpenseList component for displaying expenses
// Requirements: 5.1, 5.2, 5.3, 5.4, 5.5

import { useState, useEffect } from 'react';
import { getExpenses, updateExpense, deleteExpense } from '../services/expenseAPI';
import type { Expense } from '../types';
import EditExpenseModal from './EditExpenseModal';

interface ExpenseListProps {
  mes: string;
  aba: string;
}

export default function ExpenseList({ mes, aba }: ExpenseListProps) {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingExpense, setEditingExpense] = useState<Expense | null>(null);

  useEffect(() => {
    fetchExpenses();
  }, [mes, aba]);

  const fetchExpenses = async () => {
    setLoading(true);
    setError(null);

    try {
      const data = await getExpenses(mes, aba);
      setExpenses(data);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Erro ao carregar gastos');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (expense: Expense) => {
    setEditingExpense(expense);
  };

  const handleSaveEdit = async (descricao: string, valor: number) => {
    if (!editingExpense) return;

    await updateExpense(editingExpense.rowId, {
      descricao,
      valor,
      aba,
      mes
    });

    // Refresh list
    fetchExpenses();
  };

  const handleDelete = async (rowId: number) => {
    if (window.confirm('Tem certeza que deseja excluir este gasto?')) {
      try {
        await deleteExpense(rowId, aba);
        fetchExpenses();
      } catch (err) {
        alert(err instanceof Error ? err.message : 'Erro ao excluir');
      }
    }
  };

  const total = expenses.reduce((acc, curr) => acc + curr.valor, 0);
  const totalFormatted = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(total);

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <svg className="animate-spin h-8 w-8 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 mx-4 my-4 rounded shadow-sm">
        <div className="flex">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  if (expenses.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
        <div className="bg-gray-100 rounded-full p-4 mb-4">
          <svg className="h-8 w-8 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
          </svg>
        </div>
        <h3 className="text-lg font-medium text-gray-900">Nenhum gasto encontrado</h3>
        <p className="mt-1 text-sm text-gray-500">Comece adicionando um novo gasto para este mês.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      {/* Total Header */}
      <div className="bg-indigo-50 px-6 py-4 border-b border-indigo-100 flex justify-between items-center">
        <span className="text-sm font-medium text-indigo-800 uppercase tracking-wider">Total do Mês</span>
        <span className="text-2xl font-bold text-indigo-900 font-mono">{totalFormatted}</span>
      </div>

      {/* Desktop Table View */}
      <div className="hidden md:block overflow-x-auto flex-grow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-1/2">
                Descrição
              </th>
              <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider w-1/4">
                Valor
              </th>
              <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider w-1/4">
                Ações
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {expenses.map((expense) => (
              <tr key={expense.rowId} className="hover:bg-gray-50 transition-colors duration-150 ease-in-out group">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {expense.descricao}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right font-mono">
                  {expense.valorFormatado}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex justify-end space-x-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => handleEdit(expense)}
                      className="text-indigo-600 hover:text-indigo-900 bg-indigo-50 hover:bg-indigo-100 px-2 py-1 rounded"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => handleDelete(expense.rowId)}
                      className="text-red-600 hover:text-red-900 bg-red-50 hover:bg-red-100 px-2 py-1 rounded"
                    >
                      Excluir
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden divide-y divide-gray-200 flex-grow">
        {expenses.map((expense) => (
          <div key={expense.rowId} className="px-4 py-4 hover:bg-gray-50 active:bg-gray-100 transition-colors">
            <div className="flex justify-between items-start mb-2">
              <div className="text-sm font-medium text-gray-900 truncate pr-4">
                {expense.descricao}
              </div>
              <div className="text-sm font-semibold text-gray-900 font-mono whitespace-nowrap">
                {expense.valorFormatado}
              </div>
            </div>
            <div className="flex justify-end space-x-3 mt-2">
              <button
                onClick={() => handleEdit(expense)}
                className="text-xs font-medium text-indigo-600 hover:text-indigo-800"
              >
                Editar
              </button>
              <button
                onClick={() => handleDelete(expense.rowId)}
                className="text-xs font-medium text-red-600 hover:text-red-800"
              >
                Excluir
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Footer summary */}
      <div className="bg-gray-50 px-6 py-3 border-t border-gray-200 text-xs text-gray-500 text-right">
        Total de {expenses.length} itens listados
      </div>

      {/* Edit Modal */}
      <EditExpenseModal
        isOpen={!!editingExpense}
        onClose={() => setEditingExpense(null)}
        onSave={handleSaveEdit}
        expense={editingExpense}
      />
    </div>
  );
}
