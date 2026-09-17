-- profiles
CREATE TABLE public.profiles (
  id uuid PRIMARY KEY,
  full_name text,
  email text,
  skills text,
  location text,
  created_at timestamptz NOT NULL DEFAULT now()
);
GRANT SELECT, INSERT, UPDATE, DELETE ON public.profiles TO authenticated;
GRANT ALL ON public.profiles TO service_role;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "own profile select" ON public.profiles FOR SELECT TO authenticated USING (auth.uid() = id);
CREATE POLICY "own profile insert" ON public.profiles FOR INSERT TO authenticated WITH CHECK (auth.uid() = id);
CREATE POLICY "own profile update" ON public.profiles FOR UPDATE TO authenticated USING (auth.uid() = id);

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, email)
  VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'full_name', ''), NEW.email)
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- initiatives
CREATE TABLE public.initiatives (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  category text NOT NULL,
  goal text,
  description text,
  location text,
  date date,
  status text NOT NULL DEFAULT 'active',
  creator_id uuid NOT NULL DEFAULT auth.uid(),
  created_at timestamptz NOT NULL DEFAULT now()
);
GRANT SELECT ON public.initiatives TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.initiatives TO authenticated;
GRANT ALL ON public.initiatives TO service_role;
ALTER TABLE public.initiatives ENABLE ROW LEVEL SECURITY;
CREATE POLICY "public read active initiatives" ON public.initiatives FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY "owner insert initiative" ON public.initiatives FOR INSERT TO authenticated WITH CHECK (auth.uid() = creator_id);
CREATE POLICY "owner update initiative" ON public.initiatives FOR UPDATE TO authenticated USING (auth.uid() = creator_id);
CREATE POLICY "owner delete initiative" ON public.initiatives FOR DELETE TO authenticated USING (auth.uid() = creator_id);

-- tasks
CREATE TABLE public.tasks (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  initiative_id uuid NOT NULL REFERENCES public.initiatives(id) ON DELETE CASCADE,
  name text NOT NULL,
  category text,
  status text NOT NULL DEFAULT 'todo',
  created_at timestamptz NOT NULL DEFAULT now()
);
GRANT SELECT ON public.tasks TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.tasks TO authenticated;
GRANT ALL ON public.tasks TO service_role;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
CREATE POLICY "public read tasks" ON public.tasks FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY "owner manage tasks" ON public.tasks FOR ALL TO authenticated
  USING (EXISTS (SELECT 1 FROM public.initiatives i WHERE i.id = initiative_id AND i.creator_id = auth.uid()))
  WITH CHECK (EXISTS (SELECT 1 FROM public.initiatives i WHERE i.id = initiative_id AND i.creator_id = auth.uid()));

-- volunteer_requests
CREATE TABLE public.volunteer_requests (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  initiative_id uuid NOT NULL REFERENCES public.initiatives(id) ON DELETE CASCADE,
  name text NOT NULL,
  skill text,
  task text,
  status text NOT NULL DEFAULT 'pending',
  user_id uuid DEFAULT auth.uid(),
  created_at timestamptz NOT NULL DEFAULT now()
);
GRANT SELECT ON public.volunteer_requests TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.volunteer_requests TO authenticated;
GRANT ALL ON public.volunteer_requests TO service_role;
ALTER TABLE public.volunteer_requests ENABLE ROW LEVEL SECURITY;
CREATE POLICY "read volunteer requests" ON public.volunteer_requests FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY "authenticated insert volunteer request" ON public.volunteer_requests FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "owner update volunteer request" ON public.volunteer_requests FOR UPDATE TO authenticated
  USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.initiatives i WHERE i.id = initiative_id AND i.creator_id = auth.uid()));
CREATE POLICY "owner delete volunteer request" ON public.volunteer_requests FOR DELETE TO authenticated
  USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.initiatives i WHERE i.id = initiative_id AND i.creator_id = auth.uid()));

-- initiative_memory
CREATE TABLE public.initiative_memory (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  initiative_id uuid NOT NULL REFERENCES public.initiatives(id) ON DELETE CASCADE,
  type text NOT NULL CHECK (type IN ('decision','problem','solution','lesson')),
  text text NOT NULL,
  tag text,
  created_at timestamptz NOT NULL DEFAULT now()
);
GRANT SELECT ON public.initiative_memory TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.initiative_memory TO authenticated;
GRANT ALL ON public.initiative_memory TO service_role;
ALTER TABLE public.initiative_memory ENABLE ROW LEVEL SECURITY;
CREATE POLICY "public read memory" ON public.initiative_memory FOR SELECT TO anon, authenticated USING (true);
CREATE POLICY "owner manage memory" ON public.initiative_memory FOR ALL TO authenticated
  USING (EXISTS (SELECT 1 FROM public.initiatives i WHERE i.id = initiative_id AND i.creator_id = auth.uid()))
  WITH CHECK (EXISTS (SELECT 1 FROM public.initiatives i WHERE i.id = initiative_id AND i.creator_id = auth.uid()));
