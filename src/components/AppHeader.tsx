import { Link, useNavigate } from "@tanstack/react-router";
import { useQueryClient } from "@tanstack/react-query";
import { LogOut, Network } from "lucide-react";

import { Button } from "@/components/ui/button";
import { supabase } from "@/integrations/supabase/client";

export function AppHeader({ email }: { email?: string | null }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const signOut = async () => {
    await queryClient.cancelQueries();
    queryClient.clear();
    await supabase.auth.signOut();
    navigate({ to: "/auth", replace: true });
  };

  return (
    <header className="border-b border-border/60 bg-card">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-5 py-4">
        <Link to="/dashboard" className="flex items-center gap-2">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-primary text-primary-foreground">
            <Network className="h-5 w-5" />
          </span>
          <span className="font-display text-xl font-bold">أثر</span>
        </Link>
        <div className="flex items-center gap-3">
          {email && <span className="hidden text-sm text-muted-foreground sm:block">{email}</span>}
          <Button variant="outline" size="sm" onClick={signOut}>
            <LogOut className="h-4 w-4" />
            خروج
          </Button>
        </div>
      </div>
    </header>
  );
}
