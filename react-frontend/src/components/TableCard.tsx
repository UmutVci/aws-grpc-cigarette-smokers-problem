import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Users, Play, Lock } from "lucide-react";
import { TableStatus } from "@/api/gameApi";

interface TableCardProps {
  table: TableStatus;
  onJoin: (tableId: string) => void;
}

const TableCard = ({ table, onJoin }: TableCardProps) => {
  const isJoinable = !table.started && !table.booked;
  
  return (
    <Card className="p-6 bg-card border-border hover:border-primary/50 transition-all shadow-card hover:shadow-gaming">
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-foreground">{table.tableId}</h3>
          {table.started && (
            <div className="flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-accent/20 text-accent">
              <Play className="w-3 h-3" />
              Live
            </div>
          )}
          {table.booked && !table.started && (
            <div className="flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-muted text-muted-foreground">
              <Lock className="w-3 h-3" />
              Full
            </div>
          )}
        </div>
        
        <div className="flex items-center gap-2 text-muted-foreground">
          <Users className="w-4 h-4" />
          <span className="text-sm">{table.playerCount} {table.playerCount === 1 ? 'player' : 'players'}</span>
        </div>

        <Button
          onClick={() => onJoin(table.tableId)}
          disabled={!isJoinable}
          className={`w-full ${
            isJoinable 
              ? 'bg-primary hover:bg-primary/90 text-primary-foreground shadow-gaming' 
              : 'bg-muted text-muted-foreground cursor-not-allowed'
          }`}
        >
          {isJoinable ? 'Join Table' : table.started ? 'Game in Progress' : 'Table Full'}
        </Button>
      </div>
    </Card>
  );
};

export default TableCard;
