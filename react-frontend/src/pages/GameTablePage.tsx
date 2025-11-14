import { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import Navbar from "@/components/Navbar";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2, LogOut, Users, Play, Lock } from "lucide-react";
import { gameApi, TableStatus } from "@/api/gameApi";
import { toast } from "sonner";

const GameTablePage = () => {
  const { tableId } = useParams<{ tableId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const username = location.state?.username;
  const [logs, setLogs] = useState<string[]>([]);
  const [table, setTable] = useState<TableStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [leaving, setLeaving] = useState(false);

  useEffect(() => {
    if (!tableId) {
      navigate('/lobby');
      return;
    }

    const fetchTable = async () => {
      try {
        const data = await gameApi.getTableStatus(tableId);
        setTable(data);
      } catch (error) {
        toast.error("Failed to load table");
        console.error(error);
        navigate('/lobby');
      } finally {
        setLoading(false);
      }
    };

    fetchTable();

    // Poll every 5 seconds
    const interval = setInterval(fetchTable, 5000);
    return () => clearInterval(interval);
  }, [tableId, navigate]);

  useEffect(() => {
    if (!tableId) {
      console.log("No tableId, skipping SSE connection.");
      return;
    }

    console.log(`🔌 Connecting to SSE stream for table ${tableId}`);
    const eventSource = new EventSource(`http://localhost:8080/api/game/events/${tableId}`);

    eventSource.addEventListener("game-server", (event) => {
      const message = event.data;
      console.log("🎮 Game event received:", message);
      setLogs((prev) => [...prev, message]);
    });

    eventSource.onopen = () => {
      console.log("✅ SSE connection opened");
    };

    eventSource.onerror = (err) => {
      console.error("❌ SSE error:", err);
      // Auto-reconnect için burada logic ekleyebilirsiniz
      eventSource.close();
    };

    return () => {
      console.log("🧹 Closing SSE connection...");
      eventSource.close();
    };
  }, [tableId]);


  const handleLeave = async () => {
    if (!tableId || !username) return;

    setLeaving(true);
    try {
      await gameApi.leaveTable(username, tableId);
      toast.success("Left the table");
      navigate('/lobby');
    } catch (error) {
      toast.error("Failed to leave table");
      console.error(error);
    } finally {
      setLeaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto px-4 py-16 flex items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      </div>
    );
  }

  if (!table) return null;

  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      <main className="container mx-auto px-4 py-8">
        <div className="max-w-3xl mx-auto space-y-6">
          {/* Table Header */}
          <Card className="p-6 bg-card border-border shadow-card">
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-2xl font-bold">{table.tableId}</h1>
              <div className="flex items-center gap-2">
                {table.started && (
                  <Badge className="bg-accent/20 text-accent border-accent/30">
                    <Play className="w-3 h-3 mr-1" />
                    Live
                  </Badge>
                )}
                {table.booked && (
                  <Badge variant="secondary">
                    <Lock className="w-3 h-3 mr-1" />
                    Full
                  </Badge>
                )}
              </div>
            </div>

            <div className="flex items-center gap-2 text-muted-foreground mb-6">
              <Users className="w-5 h-5" />
              <span>{table.playerCount} {table.playerCount === 1 ? 'player' : 'players'}</span>
            </div>

            <Button
              onClick={handleLeave}
              disabled={leaving}
              variant="destructive"
              className="w-full"
            >
              {leaving ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Leaving...
                </>
              ) : (
                <>
                  <LogOut className="w-4 h-4 mr-2" />
                  Leave Table
                </>
              )}
            </Button>
          </Card>

          {/* Players List */}
          <Card className="p-6 bg-card border-border shadow-card">
            <h2 className="text-xl font-semibold mb-4">Players</h2>
            <div className="space-y-3">
              {table.players && table.players.length > 0 ? (
                table.players.map((player, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-3 rounded-lg bg-secondary"
                  >
                    <span className="font-medium">{player}</span>
                    {player === username && (
                      <Badge variant="outline" className="border-primary text-primary">
                        You
                      </Badge>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-muted-foreground text-center py-4">
                  No players yet
                </p>
              )}
            </div>
          </Card>

          <Card className="p-6 bg-card border-border shadow-card">
            <h2 className="text-xl font-semibold mb-4">Game Logs</h2>
            <div className="space-y-2 text-sm font-mono bg-secondary p-3 rounded-md h-60 overflow-y-auto">
              {logs.length > 0 ? (
                  logs.map((log, index) => (
                      <div key={index} className="text-foreground">
                        {log}
                      </div>
                  ))
              ) : (
                  <p className="text-muted-foreground">No events yet...</p>
              )}
            </div>
          </Card>

          {/* Game Status */}
          <Card className="p-6 bg-card border-border shadow-card">
            <h2 className="text-xl font-semibold mb-4">Status</h2>
            <div className="space-y-2 text-muted-foreground">
              <p>Table ID: <span className="text-foreground font-mono">{table.tableId}</span></p>
              <p>Status: <span className="text-foreground">{table.started ? 'Game in progress' : 'Waiting for players'}</span></p>
              <p>Capacity: <span className="text-foreground">{table.booked ? 'Full' : 'Open'}</span></p>
            </div>
          </Card>
        </div>
      </main>
    </div>
  );
};

export default GameTablePage;
