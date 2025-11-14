import axios, { AxiosError } from "axios";


export interface TableStatus {
  tableId: string;
  started: boolean;
  booked: boolean;
  playerCount: number;
  players?: string[];
}

export interface JoinGameResponse {
  tableId: string;
  username: string;
  success: boolean;
  message?: string;
}

const API_BASE_URL = "http://localhost:8080/api/game";

export const gameApi = {
  async joinGame(username: string): Promise<JoinGameResponse> {
    const response = await axios.post(`${API_BASE_URL}/join`, { username });
    return {
      username,
      success: true,
      message: response.data.message,
      tableId: response.data.tableId,
    };
  },

  async joinSpecificTable(username: string, tableId: string): Promise<JoinGameResponse> {
    const response = await axios.post(`${API_BASE_URL}/join-specific`, {
      username,
      tableId,
    });
    return {
      tableId,
      username,
      success: true,
      message: response.data.message,
    };
  },

  async leaveTable(username: string, tableId: string): Promise<void> {
    try {
      console.log("Sending leave request:", { username, tableId });
      const response = await axios.post(`${API_BASE_URL}/leave`, { username, tableId });
      console.log("Leave response:", response.data);
    } catch (error) {
      const err = error as AxiosError;
      console.error("Leave error:", err.response?.data || err.message);
    }
  },

  async getTableStatus(tableId: string): Promise<TableStatus> {
    const response = await axios.get(`${API_BASE_URL}/table/${tableId}`);
    return response.data;
  },

  async getAllTables(): Promise<TableStatus[]> {
    const response = await axios.get(`${API_BASE_URL}/tables`);
    return response.data;
  },
};
