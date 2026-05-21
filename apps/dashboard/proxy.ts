import { NextResponse, NextRequest } from 'next/server'
 
export function proxy(request: NextRequest) {
 
  // If the user is authenticated or dev, continue as normal
  if (request.cookies.get("auth_token")
     || process.env['NODE_ENV'] == "development"
    ) {
    return NextResponse.next()
  }
 
  // Redirect to login page if not authenticated
  return NextResponse.redirect(new URL('/login', request.url))
}
 
export const config = {
  matcher: '/dashboard/:path*',
}