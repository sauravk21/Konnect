'use client';

import { createClient } from '@/lib/supabase/client';
import { useState, useEffect } from 'react';

export default function Home() {
  const [user, setUser] = useState<any>(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isSignUp, setIsSignUp] = useState(false);
  const [health, setHealth] = useState('');
  const supabase = createClient();

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setUser(session?.user ?? null);
    });
    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null);
    });
    return () => subscription.unsubscribe();
  }, [supabase]);

  const handleAuth = async () => {
    if (isSignUp) {
      const { error } = await supabase.auth.signUp({ email, password });
      if (error) alert(error.message);
      else alert('Check your email for confirmation!');
    } else {
      const { error } = await supabase.auth.signInWithPassword({ email, password });
      if (error) alert(error.message);
    }
  };

  const handleSignOut = async () => {
    await supabase.auth.signOut();
  };

  // const checkBackend = async () => {
  //   console.log('Button clicked'); 
  //   try {
  //     const res = await fetch('/api/health');
  //     const data = await res.json();
  //     setHealth(JSON.stringify(data));
  //   } catch (error) {
  //     setHealth('Backend not reachable');
  //   }
  // };

  const checkBackend = async () => {
  const { data: { session } } = await supabase.auth.getSession();
  const token = session?.access_token;
  console.log('Token:', token);
  try {
    const res = await fetch('http://localhost:8080/api/v1/health', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await res.json();
    setHealth(JSON.stringify(data));
  } catch (error) { 
    setHealth('Error: ' + error);
  }
};

  if (user) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center p-24">
        <div>
          <p>Welcome, {user.email}</p>
          <button onClick={handleSignOut} className="bg-red-500 text-white px-4 py-2 rounded mt-2">
            Sign Out
          </button>
          <button onClick={checkBackend} className="bg-green-500 text-white px-4 py-2 rounded mt-2 ml-2">
            Check Backend Health
          </button>
          <p className="mt-4">{health}</p>
        </div>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="bg-white p-8 rounded shadow-md w-96">
        <h1 className="text-2xl font-bold mb-4">{isSignUp ? 'Sign Up' : 'Sign In'}</h1>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-full border p-2 mb-2 rounded"
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full border p-2 mb-4 rounded"
        />
        <button onClick={handleAuth} className="bg-blue-500 text-white w-full py-2 rounded mb-2">
          {isSignUp ? 'Sign Up' : 'Sign In'}
        </button>
        <button onClick={() => setIsSignUp(!isSignUp)} className="text-blue-500 text-sm w-full">
          {isSignUp ? 'Already have an account? Sign In' : 'Need an account? Sign Up'}
        </button>
      </div>
    </main>
  );
}