import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  MessageSquare,
  Database,
  Zap,
  Shield,
  BarChart3,
  Users,
} from "lucide-react"

const features = [
  {
    icon: MessageSquare,
    title: "Natural Language Queries",
    description:
      "Ask questions in plain English and get SQL queries generated automatically. No need to remember complex syntax.",
  },
  {
    icon: Database,
    title: "Multi-Database Support",
    description:
      "Connect to PostgreSQL, MySQL, MongoDB, and more. Manage all your databases from one unified interface.",
  },
  {
    icon: Zap,
    title: "Lightning Fast",
    description: "Get instant results with our optimized query engine. Execute complex queries in milliseconds.",
  },
  {
    icon: Shield,
    title: "Enterprise Security",
    description: "Bank-level encryption and security protocols. Your data stays safe and private at all times.",
  },
  {
    icon: BarChart3,
    title: "Visual Analytics",
    description: "Transform your query results into beautiful charts and graphs with just one click.",
  },
  {
    icon: Users,
    title: "Team Collaboration",
    description: "Share queries, results, and insights with your team. Built for collaborative data exploration.",
  },
]

const FeaturesSection = () => {
  return (
    <section className="py-16 px-4 sm:px-6 lg:px-8 bg-muted/30">
      <div className="max-w-7xl mx-auto">
        <div className="text-center space-y-4 mb-16">
          <Badge variant="secondary" className="w-fit mx-auto">
            Features
          </Badge>
          <h2 className="text-3xl sm:text-4xl font-bold">
            Everything you need to master your data
          </h2>
          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            QueryMate combines the power of AI with enterprise-grade database
            management to give you unprecedented control over your data.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <Card
              key={index}
              className="border-0 shadow-sm hover:shadow-md transition-shadow"
            >
              <CardContent className="p-6 space-y-4">
                <div className="h-12 w-12 bg-primary/10 rounded-lg flex items-center justify-center">
                  <feature.icon className="h-6 w-6 text-primary" />
                </div>
                <div className="space-y-2">
                  <h3 className="text-lg font-semibold">{feature.title}</h3>
                  <p className="text-muted-foreground">{feature.description}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  )
}

export default FeaturesSection
