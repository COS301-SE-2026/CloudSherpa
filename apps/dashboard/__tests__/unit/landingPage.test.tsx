import { expect, test } from 'vitest'
import { render, screen } from '@testing-library/react'
import LandingPage from '@/features/landingPage/components/landingPage'
 
test('renders get started button', () => {
  render(<LandingPage />)
  expect(screen.getByRole('button', { name: 'Get Started' })).toBeDefined()
})
