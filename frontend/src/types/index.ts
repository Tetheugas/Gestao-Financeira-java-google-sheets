// Type definitions for Gestão Financeira Pessoal
// Requirements: 1.3, 2.1, 4.5

/**
 * Represents an expense returned from the backend
 * Used for displaying expenses in the UI
 */
export interface Expense {
  descricao: string;
  valorFormatado: string; // Format: "R$ X.XXX,XX"
}

/**
 * Represents a request to create a new expense
 * Used when submitting the expense form
 */
export interface ExpenseRequest {
  descricao: string;
  valor: number;
  aba: string;
  mes: string;
}

/**
 * Represents a message response from the backend
 * Used for success/error messages
 */
export interface MessageResponse {
  message: string;
  status: string; // "success" or "error"
}
