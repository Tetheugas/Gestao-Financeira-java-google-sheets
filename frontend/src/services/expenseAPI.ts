// API service for expense management
// Requirements: 4.1, 4.2, 6.2

import axios, { AxiosError } from 'axios';
import type { Expense, ExpenseRequest, MessageResponse } from '../types';

// Configure axios instance with base URL
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Fetches expenses for a specific month and sheet tab
 * @param mes - Month name (e.g., "Fevereiro")
 * @param aba - Sheet tab name (e.g., "CartãoNubank")
 * @returns Promise with array of expenses
 * @throws Error with descriptive message on failure
 */
export const getExpenses = async (mes: string, aba: string): Promise<Expense[]> => {
  try {
    const response = await api.get<Expense[]>(`/expenses/${mes}`, {
      params: { aba },
    });
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao buscar gastos');
    throw error; // TypeScript requires this after handleAxiosError
  }
};

/**
 * Adds a new expense to the sheet
 * @param data - Expense data including description, value, tab, and month
 * @returns Promise with success message
 * @throws Error with descriptive message on failure
 */
export const addExpense = async (data: ExpenseRequest): Promise<MessageResponse> => {
  try {
    const response = await api.post<MessageResponse>('/expenses', data);
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao adicionar gasto');
    throw error; // TypeScript requires this after handleAxiosError
  }
};

/**
 * Fetches available filters (sheet names)
 * @returns Promise with array of filter names
 */
export const getFilters = async (): Promise<string[]> => {
  try {
    const response = await api.get<string[]>('/filters');
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao buscar filtros');
    throw error;
  }
};

/**
 * Creates a new filter (sheet)
 * @param name - Name of the new filter
 * @returns Promise with success message
 */
export const createFilter = async (name: string): Promise<MessageResponse> => {
  try {
    const response = await api.post<MessageResponse>('/filters', { name });
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao criar filtro');
    throw error;
  }
};

/**
 * Updates an existing expense
 * @param rowId - The row ID of the expense to update
 * @param data - The new expense data
 * @returns Promise with success message
 */
export const updateExpense = async (rowId: number, data: ExpenseRequest): Promise<MessageResponse> => {
  try {
    const response = await api.put<MessageResponse>(`/expenses/${rowId}`, data);
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao atualizar gasto');
    throw error;
  }
};

/**
 * Deletes an expense
 * @param rowId - The row ID of the expense to delete
 * @param aba - The sheet name where the expense is located
 * @returns Promise with success message
 */
export const deleteExpense = async (rowId: number, aba: string): Promise<MessageResponse> => {
  try {
    const response = await api.delete<MessageResponse>(`/expenses/${rowId}`, {
      params: { aba },
    });
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao remover gasto');
    throw error;
  }
};

export const getAuthStatus = async (): Promise<boolean> => {
  try {
    const response = await api.get<{ authenticated: boolean }>('/auth/status');
    return response.data.authenticated;
  } catch (error) {
    console.error("Failed to check auth status", error);
    return false;
  }
};

export const getAuthUrl = async (): Promise<string> => {
  try {
    const response = await api.post<{ url: string }>('/auth/login');
    return response.data.url;
  } catch (error) {
    handleAxiosError(error, 'Erro ao iniciar login');
    throw error;
  }
};

export const getSpreadsheetConfig = async (): Promise<string> => {
  try {
    const response = await api.get<{ spreadsheetId: string }>('/spreadsheet');
    return response.data.spreadsheetId;
  } catch (error) {
    handleAxiosError(error, 'Erro ao buscar ID da planilha');
    throw error;
  }
};

export const updateSpreadsheetConfig = async (spreadsheetId: string): Promise<MessageResponse> => {
  try {
    const response = await api.put<MessageResponse>('/spreadsheet', { spreadsheetId });
    return response.data;
  } catch (error) {
    handleAxiosError(error, 'Erro ao atualizar ID da planilha');
    throw error;
  }
};

export const createSpreadsheetConfig = async (): Promise<string> => {
  try {
    const response = await api.post<{ spreadsheetId: string }>('/spreadsheet');
    return response.data.spreadsheetId;
  } catch (error) {
    handleAxiosError(error, 'Erro ao criar nova planilha');
    throw error;
  }
};

/**
 * Handles axios errors and throws user-friendly error messages
 * @param error - The error object from axios
 * @param defaultMessage - Default message if no specific error is available
 */
const handleAxiosError = (error: unknown, defaultMessage: string): never => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<MessageResponse>;
    
    if (axiosError.response) {
      // Server responded with error status
      const message = axiosError.response.data?.message || defaultMessage;
      throw new Error(message);
    } else if (axiosError.request) {
      // Request was made but no response received
      throw new Error('Não foi possível conectar ao servidor. Verifique se o backend está rodando.');
    }
  }
  
  // Unknown error type
  throw new Error(defaultMessage);
};
