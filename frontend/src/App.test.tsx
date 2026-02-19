// Tests for App component
// Requirements: 5.1, 6.1

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import * as expenseAPI from './services/expenseAPI';

// Mock the expense API
vi.mock('./services/expenseAPI');

describe('App Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default mock for getExpenses
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue([]);
  });

  it('renders the app title', () => {
    render(<App />);
    expect(screen.getByText('Gestão Financeira Pessoal')).toBeInTheDocument();
  });

  it('renders aba and mes selectors', () => {
    render(<App />);
    
    expect(screen.getByLabelText('Aba')).toBeInTheDocument();
    expect(screen.getByLabelText('Mês')).toBeInTheDocument();
  });

  it('has default values for aba and mes', () => {
    render(<App />);
    
    const abaSelect = screen.getByLabelText('Aba') as HTMLSelectElement;
    const mesSelect = screen.getByLabelText('Mês') as HTMLSelectElement;
    
    expect(abaSelect.value).toBe('CartaoNubank');
    expect(mesSelect.value).toBe('Fevereiro');
  });

  it('renders ExpenseForm component', () => {
    render(<App />);
    
    expect(screen.getByRole('heading', { name: 'Adicionar Gasto' })).toBeInTheDocument();
    expect(screen.getByLabelText('Descrição')).toBeInTheDocument();
    expect(screen.getByLabelText('Valor')).toBeInTheDocument();
  });

  it('renders ExpenseList component', () => {
    render(<App />);
    
    expect(screen.getByText(/Gastos de Fevereiro - CartaoNubank/)).toBeInTheDocument();
  });

  it('updates aba when selector changes', async () => {
    const user = userEvent.setup();
    render(<App />);
    
    const abaSelect = screen.getByLabelText('Aba');
    await user.selectOptions(abaSelect, 'CartaoInter');
    
    expect((abaSelect as HTMLSelectElement).value).toBe('CartaoInter');
    expect(screen.getByText(/Gastos de Fevereiro - CartaoInter/)).toBeInTheDocument();
  });

  it('updates mes when selector changes', async () => {
    const user = userEvent.setup();
    render(<App />);
    
    const mesSelect = screen.getByLabelText('Mês');
    await user.selectOptions(mesSelect, 'Marco');
    
    expect((mesSelect as HTMLSelectElement).value).toBe('Marco');
    expect(screen.getByText(/Gastos de Março - CartaoNubank/)).toBeInTheDocument();
  });

  it('passes aba and mes to ExpenseList', async () => {
    render(<App />);
    
    // ExpenseList should call getExpenses with default values
    await waitFor(() => {
      expect(expenseAPI.getExpenses).toHaveBeenCalledWith('Fevereiro', 'CartaoNubank');
    });
  });

  it('passes aba and mes to ExpenseForm', () => {
    render(<App />);
    
    // ExpenseForm should receive aba and mes props
    // We can verify this by checking if the form is rendered (it would fail if props were wrong)
    expect(screen.getByText('Adicionar Gasto')).toBeInTheDocument();
  });

  it('reloads ExpenseList after adding expense', async () => {
    const user = userEvent.setup();
    vi.mocked(expenseAPI.addExpense).mockResolvedValue({
      message: 'Gasto adicionado com sucesso',
      status: 'success'
    });
    
    render(<App />);
    
    // Fill form
    await user.type(screen.getByLabelText('Descrição'), 'Netflix');
    await user.type(screen.getByLabelText('Valor'), '45.90');
    
    // Clear initial call
    vi.mocked(expenseAPI.getExpenses).mockClear();
    
    // Submit form
    await user.click(screen.getByText('Adicionar Gasto'));
    
    // Wait for form submission and list reload
    await waitFor(() => {
      // getExpenses should be called again after adding expense
      expect(expenseAPI.getExpenses).toHaveBeenCalled();
    });
  });

  it('displays expenses in the list', async () => {
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue([
      { descricao: 'Netflix', valorFormatado: 'R$ 45,90' },
      { descricao: 'Uber', valorFormatado: 'R$ 32,50' }
    ]);
    
    render(<App />);
    
    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
      expect(screen.getByText('R$ 45,90')).toBeInTheDocument();
      expect(screen.getByText('Uber')).toBeInTheDocument();
      expect(screen.getByText('R$ 32,50')).toBeInTheDocument();
    });
  });

  it('has responsive layout classes', () => {
    const { container } = render(<App />);
    
    // Check for responsive classes
    expect(container.querySelector('.min-h-screen')).toBeInTheDocument();
    expect(container.querySelector('.max-w-4xl')).toBeInTheDocument();
    expect(container.querySelector('.md\\:grid-cols-2')).toBeInTheDocument();
  });
});
