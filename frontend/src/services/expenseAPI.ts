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
