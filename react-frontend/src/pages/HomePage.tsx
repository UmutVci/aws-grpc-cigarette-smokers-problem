import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Gamepad2, Users, Zap } from "lucide-react";
import JoinModal from "@/components/JoinModal";
import Navbar from "@/components/Navbar";
import { gameApi } from "@/api/gameApi";
import { toast } from "sonner";

const HomePage = () => {
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [isJoining, setIsJoining] = useState(false);
  const navigate = useNavigate();

  const handleJoinGame = async (username: string) => {
    setIsJoining(true);
    try {
      const response = await gameApi.joinGame(username);
      toast.success(`Welcome, ${username}!`);
      navigate(`/table/${response.tableId}`, { state: { username } });
    } catch (error) {
      toast.error("Failed to join game. Please try again.");
      console.error(error);
    } finally {
      setIsJoining(false);
      setShowJoinModal(false);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      
      <main className="container mx-auto px-4 py-16">
        <div className="max-w-4xl mx-auto text-center space-y-8">
          {/* Hero Section */}
          <div className="space-y-4">
            <div className="flex items-center justify-center">
              <div className="p-4 rounded-2xl bg-gradient-gaming">
                <Gamepad2 className="w-16 h-16 text-primary" />
              </div>
            </div>
            
            <h1 className="text-5xl md:text-6xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
              Welcome to GameHub
            </h1>
            
            <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
              Join thousands of players in real-time multiplayer action. Create or join a table and start playing instantly.
            </p>
          </div>

          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button
              size="lg"
              onClick={() => setShowJoinModal(true)}
              className="text-lg px-8 bg-primary hover:bg-primary/90 text-primary-foreground shadow-gaming"
            >
              <Gamepad2 className="w-5 h-5 mr-2" />
              Join Game
            </Button>
            
            <Button
              size="lg"
              variant="outline"
              onClick={() => navigate('/lobby')}
              className="text-lg px-8 border-border hover:bg-secondary"
            >
              <Users className="w-5 h-5 mr-2" />
              Browse Lobby
            </Button>
          </div>

          {/* Features */}
          <div className="grid md:grid-cols-3 gap-6 mt-16">
            <div className="p-6 rounded-xl bg-card border border-border shadow-card">
              <div className="w-12 h-12 rounded-lg bg-gradient-gaming flex items-center justify-center mb-4 mx-auto">
                <Zap className="w-6 h-6 text-primary" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Instant Play</h3>
              <p className="text-sm text-muted-foreground">
                No downloads required. Join a game in seconds and start playing immediately.
              </p>
            </div>

            <div className="p-6 rounded-xl bg-card border border-border shadow-card">
              <div className="w-12 h-12 rounded-lg bg-gradient-gaming flex items-center justify-center mb-4 mx-auto">
                <Users className="w-6 h-6 text-primary" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Real-time Multiplayer</h3>
              <p className="text-sm text-muted-foreground">
                Connect with players worldwide in synchronized gameplay experiences.
              </p>
            </div>

            <div className="p-6 rounded-xl bg-card border border-border shadow-card">
              <div className="w-12 h-12 rounded-lg bg-gradient-gaming flex items-center justify-center mb-4 mx-auto">
                <Gamepad2 className="w-6 h-6 text-primary" />
              </div>
              <h3 className="text-lg font-semibold mb-2">Easy to Learn</h3>
              <p className="text-sm text-muted-foreground">
                Simple controls and intuitive gameplay make it easy for anyone to join.
              </p>
            </div>
          </div>
        </div>
      </main>

      <JoinModal
        open={showJoinModal}
        onOpenChange={setShowJoinModal}
        onJoin={handleJoinGame}
        isLoading={isJoining}
      />
    </div>
  );
};

export default HomePage;
