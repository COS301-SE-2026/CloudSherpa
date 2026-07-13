import { NextResponse, NextRequest } from 'next/server'
 
export function proxy(request: NextRequest) {
  // If the token is present or dev, continue as normal
  if (
    request.cookies.get("auth_token")
     || (process.env['NODE_ENV'] !== "production" && process.env["DISABLE_AUTH"] === "true")
    ) {
    return NextResponse.next()
  }
 
  // Redirect to login page if auth token not present
  return NextResponse.redirect(new URL('/login', request.url))
}
 
export const config = {
  matcher: ['/dashboard/:path*'],
}