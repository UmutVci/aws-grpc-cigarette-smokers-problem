import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";

interface JoinModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onJoin: (username: string) => Promise<void>;
  isLoading?: boolean;
}

const JoinModal = ({ open, onOpenChange, onJoin, isLoading }: JoinModalProps) => {
  const [username, setUsername] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (username.trim()) {
      await onJoin(username.trim());
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-card border-border shadow-card">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold">Join Game</DialogTitle>
          <DialogDescription className="text-muted-foreground">
            Enter your username to start playing
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div className="space-y-2">
            <Input
              placeholder="Enter username..."
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
              className="bg-secondary border-border focus:border-primary focus:ring-primary"
              autoFocus
            />
          </div>
          <Button
            type="submit"
            className="w-full bg-primary hover:bg-primary/90 text-primary-foreground shadow-gaming"
            disabled={!username.trim() || isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Joining...
              </>
            ) : (
              'Join Game'
            )}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default JoinModal;
