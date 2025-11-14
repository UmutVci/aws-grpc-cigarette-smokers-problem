import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "@/components/Navbar";
import TableCard from "@/components/TableCard";
import JoinModal from "@/components/JoinModal";
import { Button } from "@/components/ui/button";
import { Loader2, RefreshCw } from "lucide-react";
import { gameApi, TableStatus } from "@/api/gameApi";
import { toast } from "sonner";

const LobbyPage = () => {
  const [tables, setTables] = useState<TableStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [selectedTableId, setSelectedTableId] = useState<string | null>(null);
  const navigate = useNavigate();

  const fetchTables = async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    
    try {
      const data = await gameApi.getAllTables();
      setTables(data);
    } catch (error) {
      toast.error("Failed to load tables");
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchTables();
    
    // Poll every 5 seconds
    const interval = setInterval(() => {
      fetchTables(true);
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleJoinTable = (tableId: string) => {
    setSelectedTableId(tableId);
    setShowJoinModal(true);
  };

  const handleJoinWithUsername = async (username: string) => {
    if (!selectedTableId) return;
    
    try {
      await gameApi.joinSpecificTable(username, selectedTableId);
      toast.success(`Joined table ${selectedTableId}`);
      navigate(`/table/${selectedTableId}`, { state: { username } });
    } catch (error) {
      toast.error("Failed to join table");
      console.error(error);
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

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      
      <main className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold mb-2">Game Lobby</h1>
            <p className="text-muted-foreground">
              {tables.length} {tables.length === 1 ? 'table' : 'tables'} available
            </p>
          </div>
          
          <Button
            onClick={() => fetchTables(true)}
            disabled={refreshing}
            variant="outline"
            className="border-border hover:bg-secondary"
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
        </div>

        {tables.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-lg text-muted-foreground mb-4">No tables available</p>
            <Button
              onClick={() => navigate('/')}
              className="bg-primary hover:bg-primary/90 text-primary-foreground shadow-gaming"
            >
              Create New Game
            </Button>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {tables.map((table) => (
              <TableCard
                key={table.tableId}
                table={table}
                onJoin={handleJoinTable}
              />
            ))}
          </div>
        )}
      </main>

      <JoinModal
        open={showJoinModal}
        onOpenChange={setShowJoinModal}
        onJoin={handleJoinWithUsername}
      />
    </div>
  );
};

export default LobbyPage;
