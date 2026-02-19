// Unit tests for ExpenseForm component
// Requirements: 6.1, 6.2, 6.3, 6.4, 6.5

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExpenseForm from './ExpenseForm';
import * as expenseAPI from '../services/expenseAPI';

// Mock the expenseAPI module
vi.mock('../services/expenseAPI', () => ({
  addExpense: vi.fn(),
}));

describe('ExpenseForm', () => {
  const mockOnExpenseAdded = vi.fn();
  const defaultProps = {
    aba: 'CartãoNubank',
    mes: 'Fevereiro',
    onExpenseAdded: mockOnExpenseAdded,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render form with all fields', () => {
    render(<ExpenseForm {...defaultProps} />);

    expect(screen.getByRole('heading', { name: 'Adicionar Gasto' })).toBeInTheDocument();
    expect(screen.getByLabelText('Descrição')).toBeInTheDocument();
    expect(screen.getByLabelText('Valor (R$)')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Adicionar' })).toBeInTheDocument();
  });

  it('should update descricao field when user types', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    await user.type(descricaoInput, 'Netflix');

    expect(descricaoInput).toHaveValue('Netflix');
  });

  it('should update valor field when user types numeric values', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const valorInput = screen.getByLabelText('Valor (R$)');
    await user.type(valorInput, '45.90');

    expect(valorInput).toHaveValue('45.90');
  });

  it('should filter non-numeric characters from valor field', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const valorInput = screen.getByLabelText('Valor (R$)');
    await user.type(valorInput, 'abc123.45xyz');

    expect(valorInput).toHaveValue('123.45');
  });

  it('should allow comma and period in valor field', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const valorInput = screen.getByLabelText('Valor (R$)');
    await user.type(valorInput, '1.234,56');

    expect(valorInput).toHaveValue('1.234,56');
  });

  it('should show validation error when descricao is empty', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const valorInput = screen.getByLabelText('Valor (R$)');
    await user.type(valorInput, '100');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    expect(await screen.findByText('Descrição é obrigatória')).toBeInTheDocument();
    expect(expenseAPI.addExpense).not.toHaveBeenCalled();
  });

  it('should show validation error when valor is empty', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    await user.type(descricaoInput, 'Netflix');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    expect(await screen.findByText('Valor deve ser um número válido')).toBeInTheDocument();
    expect(expenseAPI.addExpense).not.toHaveBeenCalled();
  });

  it('should show validation error when valor is not a number', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    await user.type(descricaoInput, 'Netflix');

    // Manually set invalid value (bypassing filter)
    const valorInput = screen.getByLabelText('Valor (R$)') as HTMLInputElement;
    valorInput.value = 'invalid';

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    expect(await screen.findByText('Valor deve ser um número válido')).toBeInTheDocument();
    expect(expenseAPI.addExpense).not.toHaveBeenCalled();
  });

  it('should submit form with valid data', async () => {
    const user = userEvent.setup();
    vi.mocked(expenseAPI.addExpense).mockResolvedValue({
      message: 'Gasto adicionado com sucesso',
      status: 'success',
    });

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, 'Netflix');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    await waitFor(() => {
      expect(expenseAPI.addExpense).toHaveBeenCalledWith({
        descricao: 'Netflix',
        valor: 45.90,
        aba: 'CartãoNubank',
        mes: 'Fevereiro',
      });
    });
  });

  it('should clear form after successful submission', async () => {
    const user = userEvent.setup();
    vi.mocked(expenseAPI.addExpense).mockResolvedValue({
      message: 'Gasto adicionado com sucesso',
      status: 'success',
    });

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, 'Netflix');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    await waitFor(() => {
      expect(descricaoInput).toHaveValue('');
      expect(valorInput).toHaveValue('');
    });
  });

  it('should call onExpenseAdded callback after successful submission', async () => {
    const user = userEvent.setup();
    vi.mocked(expenseAPI.addExpense).mockResolvedValue({
      message: 'Gasto adicionado com sucesso',
      status: 'success',
    });

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, 'Netflix');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockOnExpenseAdded).toHaveBeenCalledTimes(1);
    });
  });

  it('should display error message from backend', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Aba não encontrada';
    vi.mocked(expenseAPI.addExpense).mockRejectedValue(new Error(errorMessage));

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, 'Netflix');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    expect(await screen.findByText(errorMessage)).toBeInTheDocument();
    expect(mockOnExpenseAdded).not.toHaveBeenCalled();
  });

  it('should show loading state while submitting', async () => {
    const user = userEvent.setup();
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    vi.mocked(expenseAPI.addExpense).mockReturnValue(promise as any);

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, 'Netflix');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    // Check loading state
    expect(screen.getByRole('button', { name: 'Adicionando...' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Adicionando...' })).toBeDisabled();

    // Resolve promise
    resolvePromise!({ message: 'Success', status: 'success' });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Adicionar' })).toBeInTheDocument();
    });
  });

  it('should disable inputs while submitting', async () => {
    const user = userEvent.setup();
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    vi.mocked(expenseAPI.addExpense).mockReturnValue(promise as any);

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, 'Netflix');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    // Check inputs are disabled
    expect(descricaoInput).toBeDisabled();
    expect(valorInput).toBeDisabled();

    // Resolve promise
    resolvePromise!({ message: 'Success', status: 'success' });

    await waitFor(() => {
      expect(descricaoInput).not.toBeDisabled();
      expect(valorInput).not.toBeDisabled();
    });
  });

  it('should trim whitespace from descricao before submitting', async () => {
    const user = userEvent.setup();
    vi.mocked(expenseAPI.addExpense).mockResolvedValue({
      message: 'Gasto adicionado com sucesso',
      status: 'success',
    });

    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, '  Netflix  ');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    await waitFor(() => {
      expect(expenseAPI.addExpense).toHaveBeenCalledWith({
        descricao: 'Netflix',
        valor: 45.90,
        aba: 'CartãoNubank',
        mes: 'Fevereiro',
      });
    });
  });

  it('should show validation error for whitespace-only descricao', async () => {
    const user = userEvent.setup();
    render(<ExpenseForm {...defaultProps} />);

    const descricaoInput = screen.getByLabelText('Descrição');
    const valorInput = screen.getByLabelText('Valor (R$)');

    await user.type(descricaoInput, '   ');
    await user.type(valorInput, '45.90');

    const submitButton = screen.getByRole('button', { name: 'Adicionar' });
    await user.click(submitButton);

    expect(await screen.findByText('Descrição é obrigatória')).toBeInTheDocument();
    expect(expenseAPI.addExpense).not.toHaveBeenCalled();
  });
});
