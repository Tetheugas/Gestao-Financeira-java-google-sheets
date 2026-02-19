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
    // Default mock for getFilters
    vi.mocked(expenseAPI.getFilters).mockResolvedValue(['CartaoNubank', 'CartaoInter', 'CartaoSantander', 'Dinheiro']);
    // Default mock for auth
    vi.mocked(expenseAPI.getAuthStatus).mockResolvedValue(true);
  });

  it('renders the app title', () => {
    render(<App />);
    expect(screen.getByText('Gestão Financeira')).toBeInTheDocument();
  });

  it('renders aba and mes selectors', async () => {
    render(<App />);
    
    expect(screen.getByLabelText('Conta / Cartão')).toBeInTheDocument();
    expect(screen.getByLabelText('Mês de Referência')).toBeInTheDocument();
  });

  it('has default values for aba and mes', async () => {
    render(<App />);
    
    // Wait for filters to load
    await waitFor(() => {
        expect(expenseAPI.getFilters).toHaveBeenCalled();
    });

    const abaSelect = screen.getByLabelText('Conta / Cartão') as HTMLSelectElement;
    const mesSelect = screen.getByLabelText('Mês de Referência') as HTMLSelectElement;
    
    expect(abaSelect.value).toBe('CartaoNubank');
    expect(mesSelect.value).toBe('Fevereiro');
  });

  it('renders ExpenseForm component', () => {
    render(<App />);
    
    expect(screen.getByRole('heading', { name: 'Adicionar Gasto' })).toBeInTheDocument();
    expect(screen.getByLabelText('Descrição')).toBeInTheDocument();
    expect(screen.getByLabelText('Valor (R$)')).toBeInTheDocument();
  });

  it('renders ExpenseList component', async () => {
    render(<App />);
    
    // Wait for initial render
    await waitFor(() => {
        expect(screen.getByText('Fevereiro • CartaoNubank')).toBeInTheDocument();
    });
  });

  it('updates aba when selector changes', async () => {
    const user = userEvent.setup();
    render(<App />);
    
    // Wait for filters to load
    await waitFor(() => {
        expect(expenseAPI.getFilters).toHaveBeenCalled();
    });

    const abaSelect = screen.getByLabelText('Conta / Cartão');
    await user.selectOptions(abaSelect, 'CartaoInter');
    
    expect((abaSelect as HTMLSelectElement).value).toBe('CartaoInter');
    expect(screen.getByText('Fevereiro • CartaoInter')).toBeInTheDocument();
  });

  it('updates mes when selector changes', async () => {
    const user = userEvent.setup();
    render(<App />);
    
    const mesSelect = screen.getByLabelText('Mês de Referência');
    await user.selectOptions(mesSelect, 'Marco');
    
    expect((mesSelect as HTMLSelectElement).value).toBe('Marco');
    expect(screen.getByText('Marco • CartaoNubank')).toBeInTheDocument();
  });

  it('passes aba and mes to ExpenseList', async () => {
    render(<App />);
    
    // ExpenseList should call getExpenses with default values
    await waitFor(() => {
      expect(expenseAPI.getExpenses).toHaveBeenCalledWith('Fevereiro', 'CartaoNubank');
    });
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
    await user.type(screen.getByLabelText('Valor (R$)'), '45.90');
    
    // Clear initial call
    vi.mocked(expenseAPI.getExpenses).mockClear();
    
    // Submit form
    await user.click(screen.getByText('Adicionar'));
    
    // Wait for form submission and list reload
    await waitFor(() => {
      // getExpenses should be called again after adding expense
      expect(expenseAPI.getExpenses).toHaveBeenCalled();
    });
  });

  it('displays expenses in the list', async () => {
    vi.mocked(expenseAPI.getExpenses).mockResolvedValue([
      { rowId: 1, descricao: 'Netflix', valorFormatado: 'R$ 45,90', valor: 45.90 },
      { rowId: 2, descricao: 'Uber', valorFormatado: 'R$ 32,50', valor: 32.50 }
    ]);
    
    render(<App />);
    
    await waitFor(() => {
      const netflixItems = screen.getAllByText('Netflix');
      expect(netflixItems.length).toBeGreaterThan(0);

      const valueItems = screen.getAllByText('R$ 45,90');
      expect(valueItems.length).toBeGreaterThan(0);

      const uberItems = screen.getAllByText('Uber');
      expect(uberItems.length).toBeGreaterThan(0);

      const uberValueItems = screen.getAllByText('R$ 32,50');
      expect(uberValueItems.length).toBeGreaterThan(0);
    });
  });

  it('has responsive layout classes', () => {
    const { container } = render(<App />);
    
    // Check for responsive classes
    expect(container.querySelector('.min-h-screen')).toBeInTheDocument();
    expect(container.querySelector('.max-w-7xl')).toBeInTheDocument();
    expect(container.querySelector('.lg\\:grid-cols-3')).toBeInTheDocument();
  });
});
