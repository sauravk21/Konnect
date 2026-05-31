import { createServerClient } from '@supabase/ssr'
import { cookies } from 'next/headers'
import { NextResponse } from 'next/server'

export async function GET() {
  const cookieStore = await cookies()
  
  const supabase = createServerClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      cookies: {
        get(name: string) {
          return cookieStore.get(name)?.value
        },
        set() {},
        remove() {},
      },
    }
  )

  const { data: { session } } = await supabase.auth.getSession()
  const token = session?.access_token

  if (!token) {
    return NextResponse.json({ error: 'Not authenticated' }, { status: 401 })
  }

  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL
  console.log('Backend URL:', backendUrl)  // Add log

  try {
    const res = await fetch(`${backendUrl}/api/v1/health`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const data = await res.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Fetch error:', error)  // Add log
    return NextResponse.json({ error: 'Backend not reachable', details: String(error) }, { status: 500 })
  }
}