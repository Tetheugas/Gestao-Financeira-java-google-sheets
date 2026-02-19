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

    if (!valor || isNaN(parseFloat(valor))) {
      setError('Valor deve ser um número válido');
      return;
    }

    try {
      setSubmitting(true);
      await addExpense({
        descricao: descricao.trim(),
        valor: parseFloat(valor),
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
    <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow border border-gray-200">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Adicionar Gasto</h2>
      
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <div className="space-y-4">
        <div>
          <label htmlFor="descricao" className="block text-sm font-medium text-gray-700 mb-1">
            Descrição
          </label>
          <input
            id="descricao"
            type="text"
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Ex: Netflix, Uber, Mercado"
            disabled={submitting}
          />
        </div>

        <div>
          <label htmlFor="valor" className="block text-sm font-medium text-gray-700 mb-1">
            Valor
          </label>
          <input
            id="valor"
            type="text"
            value={valor}
            onChange={(e) => {
              // Only allow numeric characters, comma, and period
              const filtered = e.target.value.replace(/[^0-9.,]/g, '');
              setValor(filtered);
            }}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Ex: 45.90 ou 45,90"
            disabled={submitting}
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          {submitting ? 'Adicionando...' : 'Adicionar Gasto'}
        </button>
      </div>
    </form>
  );
}
