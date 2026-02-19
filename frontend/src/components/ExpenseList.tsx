// ExpenseList component for displaying expenses
// Requirements: 5.1, 5.2, 5.3, 5.4, 5.5

import { useState, useEffect } from 'react';
import { getExpenses } from '../services/expenseAPI';
import type { Expense } from '../types';

interface ExpenseListProps {
  mes: string;
  aba: string;
}

export default function ExpenseList({ mes, aba }: ExpenseListProps) {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

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

  if (loading) {
    return (
      <div className="flex justify-center items-center py-8">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {error}
      </div>
    );
  }

  if (expenses.length === 0) {
    return (
      <div className="bg-gray-50 border border-gray-200 text-gray-600 px-4 py-3 rounded text-center">
        Nenhum gasto encontrado
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full bg-white border border-gray-200 rounded-lg shadow">
        <thead className="bg-gray-100">
          <tr>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 border-b">
              Descrição
            </th>
            <th className="px-6 py-3 text-left text-sm font-semibold text-gray-700 border-b">
              Valor
            </th>
          </tr>
        </thead>
        <tbody>
          {expenses.map((expense, index) => (
            <tr
              key={index}
              className="hover:bg-gray-50 transition-colors"
            >
              <td className="px-6 py-4 text-sm text-gray-800 border-b">
                {expense.descricao}
              </td>
              <td className="px-6 py-4 text-sm text-gray-800 border-b">
                {expense.valorFormatado}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
