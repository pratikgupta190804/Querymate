import { Database } from "lucide-react"

const Footer = () => {
  return (
    <footer className="bg-muted/50 border-t">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid md:grid-cols-4 gap-8">
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <Database className="h-5 w-5" />
              </div>
              <span className="text-xl font-bold">QueryMate</span>
            </div>
            <p className="text-muted-foreground">
              Transform your database interactions with AI-powered natural language queries.
            </p>
          </div>

          <div className="space-y-4">
            <h4 className="font-semibold">Product</h4>
            <div className="space-y-2 text-sm text-muted-foreground">
              <p>Features</p>
              <p>Pricing</p>
              <p>Documentation</p>
              <p>API Reference</p>
            </div>
          </div>

          <div className="space-y-4">
            <h4 className="font-semibold">Company</h4>
            <div className="space-y-2 text-sm text-muted-foreground">
              <p>About</p>
              <p>Blog</p>
              <p>Careers</p>
              <p>Contact</p>
            </div>
          </div>

          <div className="space-y-4">
            <h4 className="font-semibold">Support</h4>
            <div className="space-y-2 text-sm text-muted-foreground">
              <p>Help Center</p>
              <p>Community</p>
              <p>Status</p>
              <p>Security</p>
            </div>
          </div>
        </div>

        <div className="border-t mt-12 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-muted-foreground">© 2024 QueryMate. All rights reserved.</p>
          <div className="flex gap-6 text-sm text-muted-foreground">
            <span>Privacy Policy</span>
            <span>Terms of Service</span>
            <span>Cookie Policy</span>
          </div>
        </div>
      </div>
    </footer>
  )
}

export default Footer
