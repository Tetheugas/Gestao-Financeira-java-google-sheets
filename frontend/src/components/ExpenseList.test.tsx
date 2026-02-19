// Unit tests for ExpenseList component
// Requirements: 5.1, 5.2, 5.3, 5.4, 5.5

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import ExpenseList from './ExpenseList';
import * as expenseAPI from '../services/expenseAPI';
import type { Expense } from '../types';

// Mock the expenseAPI module
vi.mock('../services/expenseAPI');

describe('ExpenseList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading spinner while fetching expenses', () => {
    // Mock getExpenses to return a pending promise
    vi.mocked(expenseAPI.getExpenses).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    // Check for loading spinner by class
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
  });

  it('should display expenses in a table when data is loaded', async () => {
    const mockExpenses: Expense[] = [
      { descricao: 'Netflix', valorFormatado: 'R$ 45,90' },
      { descricao: 'Uber', valorFormatado: 'R$ 32,50' },
    ];

    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpenses);

    render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    // Wait for expenses to load
    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
    });

    expect(screen.getByText('Netflix')).toBeInTheDocument();
    expect(screen.getByText('R$ 45,90')).toBeInTheDocument();
    expect(screen.getByText('Uber')).toBeInTheDocument();
    expect(screen.getByText('R$ 32,50')).toBeInTheDocument();
  });

  it('should display table headers correctly', async () => {
    const mockExpenses: Expense[] = [
      { descricao: 'Test', valorFormatado: 'R$ 100,00' },
    ];

    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpenses);

    render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('Test')).toBeInTheDocument();
    });

    expect(screen.getByText('Descrição')).toBeInTheDocument();
    expect(screen.getByText('Valor')).toBeInTheDocument();
  });

  it('should display "Nenhum gasto encontrado" when expenses list is empty', async () => {
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue([]);

    render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('Nenhum gasto encontrado')).toBeInTheDocument();
    });
  });

  it('should display error message when fetch fails', async () => {
    const errorMessage = 'Aba não encontrada';
    vi.mocked(expenseAPI.getExpenses).mockRejectedValue(new Error(errorMessage));

    render(<ExpenseList mes="Fevereiro" aba="InvalidAba" />);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should display generic error message for unknown errors', async () => {
    vi.mocked(expenseAPI.getExpenses).mockRejectedValue('Unknown error');

    render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('Erro ao carregar gastos')).toBeInTheDocument();
    });
  });

  it('should refetch expenses when mes prop changes', async () => {
    const mockExpensesFev: Expense[] = [
      { descricao: 'Netflix', valorFormatado: 'R$ 45,90' },
    ];
    const mockExpensesMar: Expense[] = [
      { descricao: 'Spotify', valorFormatado: 'R$ 19,90' },
    ];

    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpensesFev);

    const { rerender } = render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
    });

    // Change mes prop
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpensesMar);
    rerender(<ExpenseList mes="Março" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('Spotify')).toBeInTheDocument();
    });

    expect(expenseAPI.getExpenses).toHaveBeenCalledWith('Março', 'CartãoNubank');
  });

  it('should refetch expenses when aba prop changes', async () => {
    const mockExpensesNubank: Expense[] = [
      { descricao: 'Netflix', valorFormatado: 'R$ 45,90' },
    ];
    const mockExpensesInter: Expense[] = [
      { descricao: 'Amazon', valorFormatado: 'R$ 99,00' },
    ];

    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpensesNubank);

    const { rerender } = render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
    });

    // Change aba prop
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpensesInter);
    rerender(<ExpenseList mes="Fevereiro" aba="CartãoInter" />);

    await waitFor(() => {
      expect(screen.getByText('Amazon')).toBeInTheDocument();
    });

    expect(expenseAPI.getExpenses).toHaveBeenCalledWith('Fevereiro', 'CartãoInter');
  });

  it('should preserve BRL formatting from backend', async () => {
    const mockExpenses: Expense[] = [
      { descricao: 'Mercado', valorFormatado: 'R$ 1.234,56' },
    ];

    vi.mocked(expenseAPI.getExpenses).mockResolvedValue(mockExpenses);

    render(<ExpenseList mes="Fevereiro" aba="CartãoNubank" />);

    await waitFor(() => {
      expect(screen.getByText('R$ 1.234,56')).toBeInTheDocument();
    });

    // Verify exact formatting is preserved
    const valorCell = screen.getByText('R$ 1.234,56');
    expect(valorCell.textContent).toBe('R$ 1.234,56');
  });

  it('should call getExpenses with correct parameters', async () => {
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue([]);

    render(<ExpenseList mes="Janeiro" aba="CartãoXYZ" />);

    await waitFor(() => {
      expect(expenseAPI.getExpenses).toHaveBeenCalledWith('Janeiro', 'CartãoXYZ');
    });
  });
});
