import { useState, useEffect, type FormEvent } from 'react';
import type { Expense } from '../types';

interface EditExpenseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (descricao: string, valor: number) => Promise<void>;
  expense: Expense | null;
}

export default function EditExpenseModal({ isOpen, onClose, onSave, expense }: EditExpenseModalProps) {
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (expense) {
      setDescricao(expense.descricao);
      setValor(expense.valor.toFixed(2).replace('.', ','));
    }
  }, [expense]);

  if (!isOpen) return null;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!descricao.trim()) {
      setError('Descrição é obrigatória');
      return;
    }

    // Convert comma to dot for parsing
    const numericValue = parseFloat(valor.replace(',', '.'));
    if (!valor || isNaN(numericValue)) {
      setError('Valor deve ser um número válido');
      return;
    }

    try {
      setSubmitting(true);
      await onSave(descricao.trim(), numericValue);
      onClose();
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Erro ao salvar alterações');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6" aria-labelledby="modal-title" role="dialog" aria-modal="true">
      {/* Background overlay */}
      <div
        className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
        aria-hidden="true"
        onClick={onClose}
      ></div>

      {/* Modal Panel */}
      <div className="bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:max-w-lg sm:w-full z-10">
        <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div className="sm:flex sm:items-start">
            <div className="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-indigo-100 sm:mx-0 sm:h-10 sm:w-10">
              <svg className="h-6 w-6 text-indigo-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
              </svg>
            </div>
            <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left w-full">
              <h3 className="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                Editar Gasto
              </h3>

              {error && (
                <div className="mt-2 bg-red-50 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded text-sm">
                  {error}
                </div>
              )}

              <form id="edit-form" onSubmit={handleSubmit} className="mt-4 space-y-4">
                <div>
                  <label htmlFor="edit-descricao" className="block text-sm font-medium text-gray-700 mb-1">
                    Descrição
                  </label>
                  <input
                    id="edit-descricao"
                    type="text"
                    value={descricao}
                    onChange={(e) => setDescricao(e.target.value)}
                    className="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2 px-3 border"
                    placeholder="Ex: Netflix"
                  />
                </div>

                <div>
                  <label htmlFor="edit-valor" className="block text-sm font-medium text-gray-700 mb-1">
                    Valor (R$)
                  </label>
                  <div className="relative rounded-md shadow-sm">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <span className="text-gray-500 sm:text-sm">R$</span>
                    </div>
                    <input
                      id="edit-valor"
                      type="text"
                      value={valor}
                      onChange={(e) => {
                          const filtered = e.target.value.replace(/[^0-9.,]/g, '');
                          setValor(filtered);
                      }}
                      className="block w-full rounded-md border-gray-300 pl-10 focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm py-2 px-3 border"
                      placeholder="0,00"
                    />
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
        <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
          <button
            type="submit"
            form="edit-form"
            disabled={submitting}
            className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:ml-3 sm:w-auto sm:text-sm disabled:bg-gray-400"
          >
            {submitting ? 'Salvando...' : 'Salvar'}
          </button>
          <button
            type="button"
            onClick={onClose}
            disabled={submitting}
            className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
          >
            Cancelar
          </button>
        </div>
      </div>
    </div>
  );
}
