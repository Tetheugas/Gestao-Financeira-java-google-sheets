// Unit tests for expenseAPI service
// Requirements: 4.1, 4.2, 6.2

import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import type { Expense, ExpenseRequest, MessageResponse } from '../types';

// Mock axios module
vi.mock('axios', () => {
  return {
    default: {
      create: vi.fn(() => ({
        get: vi.fn(),
        post: vi.fn(),
      })),
      isAxiosError: vi.fn(),
    },
  };
});

describe('expenseAPI', () => {
  let mockGet: any;
  let mockPost: any;

  beforeEach(async () => {
    // Reset mocks before each test
    vi.clearAllMocks();
    
    // Setup fresh mock functions
    mockGet = vi.fn();
    mockPost = vi.fn();
    
    // Configure axios.create to return our mocked instance
    (axios.create as any).mockReturnValue({
      get: mockGet,
      post: mockPost,
    });
    
    // Reset the module to get a fresh instance
    vi.resetModules();
  });

  describe('getExpenses', () => {
    it('should fetch expenses successfully', async () => {
      const mockExpenses: Expense[] = [
        { rowId: 1, descricao: 'Netflix', valorFormatado: 'R$ 45,90', valor: 45.90 },
        { rowId: 2, descricao: 'Uber', valorFormatado: 'R$ 32,50', valor: 32.50 },
      ];

      mockGet.mockResolvedValue({ data: mockExpenses });
      
      // Re-import to get fresh instance with new mocks
      const { getExpenses } = await import('./expenseAPI');
      const result = await getExpenses('Fevereiro', 'CartãoNubank');

      expect(mockGet).toHaveBeenCalledWith('/expenses/Fevereiro', {
        params: { aba: 'CartãoNubank' },
      });
      expect(result).toEqual(mockExpenses);
    });

    it('should throw error when server returns error response', async () => {
      const mockError = {
        response: {
          data: { message: 'Aba não encontrada', status: 'error' },
        },
        isAxiosError: true,
      };

      mockGet.mockRejectedValue(mockError);
      (axios.isAxiosError as any).mockReturnValue(true);

      const { getExpenses } = await import('./expenseAPI');
      await expect(getExpenses('Fevereiro', 'InvalidAba')).rejects.toThrow('Aba não encontrada');
    });

    it('should throw connection error when no response received', async () => {
      const mockError = {
        request: {},
        isAxiosError: true,
      };

      mockGet.mockRejectedValue(mockError);
      (axios.isAxiosError as any).mockReturnValue(true);

      const { getExpenses } = await import('./expenseAPI');
      await expect(getExpenses('Fevereiro', 'CartãoNubank')).rejects.toThrow(
        'Não foi possível conectar ao servidor'
      );
    });
  });

  describe('addExpense', () => {
    it('should add expense successfully', async () => {
      const mockRequest: ExpenseRequest = {
        descricao: 'Mercado',
        valor: 450.0,
        aba: 'CartãoNubank',
        mes: 'Fevereiro',
      };

      const mockResponse: MessageResponse = {
        message: 'Gasto adicionado com sucesso',
        status: 'success',
      };

      mockPost.mockResolvedValue({ data: mockResponse });

      const { addExpense } = await import('./expenseAPI');
      const result = await addExpense(mockRequest);

      expect(mockPost).toHaveBeenCalledWith('/expenses', mockRequest);
      expect(result).toEqual(mockResponse);
    });

    it('should throw validation error for invalid data', async () => {
      const mockRequest: ExpenseRequest = {
        descricao: '',
        valor: 100,
        aba: 'CartãoNubank',
        mes: 'Fevereiro',
      };

      const mockError = {
        response: {
          data: { message: 'Descrição é obrigatória', status: 'error' },
        },
        isAxiosError: true,
      };

      mockPost.mockRejectedValue(mockError);
      (axios.isAxiosError as any).mockReturnValue(true);

      const { addExpense } = await import('./expenseAPI');
      await expect(addExpense(mockRequest)).rejects.toThrow('Descrição é obrigatória');
    });

    it('should use default error message when server message is unavailable', async () => {
      const mockRequest: ExpenseRequest = {
        descricao: 'Test',
        valor: 100,
        aba: 'CartãoNubank',
        mes: 'Fevereiro',
      };

      const mockError = {
        response: {
          data: {},
        },
        isAxiosError: true,
      };

      mockPost.mockRejectedValue(mockError);
      (axios.isAxiosError as any).mockReturnValue(true);

      const { addExpense } = await import('./expenseAPI');
      await expect(addExpense(mockRequest)).rejects.toThrow('Erro ao adicionar gasto');
    });
  });

  describe('axios configuration', () => {
    it('should configure axios with correct base URL', async () => {
      // Import will trigger axios.create
      await import('./expenseAPI');
      
      expect(axios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
        })
      );
    });

    it('should configure axios with JSON content type header', async () => {
      await import('./expenseAPI');
      
      expect(axios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: {
            'Content-Type': 'application/json',
          },
        })
      );
    });
  });
});
