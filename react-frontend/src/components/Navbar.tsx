import { Link, useLocation } from "react-router-dom";
import { Gamepad2, Users } from "lucide-react";

const Navbar = () => {
  const location = useLocation();
  
  const isActive = (path: string) => location.pathname === path;
  
  return (
    <nav className="border-b border-border bg-card/50 backdrop-blur-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 group">
            <div className="p-2 rounded-lg bg-gradient-gaming">
              <Gamepad2 className="w-6 h-6 text-primary" />
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
              GameHub
            </span>
          </Link>
          
          <div className="flex items-center gap-4">
            <Link
              to="/lobby"
              className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-all ${
                isActive('/lobby')
                  ? 'bg-primary text-primary-foreground shadow-gaming'
                  : 'hover:bg-secondary text-muted-foreground hover:text-foreground'
              }`}
            >
              <Users className="w-4 h-4" />
              <span className="font-medium">Lobby</span>
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
