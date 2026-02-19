// ExpenseForm component for adding new expenses
// Requirements: 6.1, 6.2, 6.3, 6.4

import { useState, FormEvent } from 'react';
import { addExpense } from '../services/expenseAPI';

interface ExpenseFormProps {
  aba: string;
  mes: string;
  onExpenseAdded: () => void;
}

export default function ExpenseForm({ aba, mes, onExpenseAdded }: ExpenseFormProps) {
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    // Local validation
    if (!descricao.trim()) {
      setError('Descrição é obrigatória');
      return;
    }

    if (!valor || isNaN(parseFloat(valor.replace(',', '.')))) {
      setError('Valor deve ser um número válido');
      return;
    }

    try {
      setSubmitting(true);
      await addExpense({
        descricao: descricao.trim(),
        valor: parseFloat(valor.replace(',', '.')),
        aba,
        mes,
      });

      // Clear form on success
      setDescricao('');
      setValor('');
      
      // Notify parent component
      onExpenseAdded();
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Erro ao adicionar gasto');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
      <h2 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-100 pb-2">
        Adicionar Gasto
      </h2>
      
      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded text-sm mb-4">
          {error}
        </div>
      )}

      <div className="space-y-4">
        <div>
          <label htmlFor="descricao" className="block text-sm font-medium text-gray-700 mb-1">
            Descrição
          </label>
          <div className="relative">
            <input
              id="descricao"
              type="text"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              className="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 px-3 bg-gray-50 border"
              placeholder="Ex: Netflix"
              disabled={submitting}
            />
          </div>
        </div>

        <div>
          <label htmlFor="valor" className="block text-sm font-medium text-gray-700 mb-1">
            Valor (R$)
          </label>
          <div className="relative">
             <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <span className="text-gray-500 sm:text-sm">R$</span>
             </div>
            <input
              id="valor"
              type="text"
              value={valor}
              onChange={(e) => {
                // Only allow numeric characters, comma, and period
                const filtered = e.target.value.replace(/[^0-9.,]/g, '');
                setValor(filtered);
              }}
              className="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2.5 pl-10 pr-3 bg-gray-50 border"
              placeholder="0,00"
              disabled={submitting}
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all duration-200"
        >
          {submitting ? (
            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
          ) : null}
          {submitting ? 'Adicionando...' : 'Adicionar'}
        </button>
      </div>
    </form>
  );
}
