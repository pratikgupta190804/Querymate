import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ArrowRight, BarChart3, Database, MessageSquare, Star } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const HeroSection = () => {
  return (
    <section className="pt-24 pb-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-8">
            <div className="space-y-4">
              <Badge variant="secondary" className="w-fit">
                🚀 AI-Powered Database Queries
              </Badge>
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight">
                Query Your Database with{" "}
                <span className="text-primary">Natural Language</span>
              </h1>
              <p className="text-xl text-muted-foreground max-w-2xl">
                Transform the way you interact with databases. Ask questions in
                plain English and get instant SQL queries, results, and insights
                powered by AI.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row gap-4">
              <Button size="lg" className="gap-2">
                Get Started Free <ArrowRight className="h-4 w-4" />
              </Button>
              <Button size="lg" variant="outline">
                Watch Demo
              </Button>
            </div>

            <div className="flex items-center gap-8 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <div className="flex -space-x-2">
                  {[1, 2, 3, 4].map((i) => (
                    <Avatar key={i} className="h-6 w-6 border-2 border-background">
                      <AvatarImage src={`/placeholder.svg?height=24&width=24`} />
                      <AvatarFallback className="text-xs">U{i}</AvatarFallback>
                    </Avatar>
                  ))}
                </div>
                <span>10,000+ developers</span>
              </div>
              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((i) => (
                  <Star key={i} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                ))}
                <span>4.9/5 rating</span>
              </div>
            </div>
          </div>

          {/* Visual Query Preview */}
          <div className="relative">
            <div className="relative bg-gradient-to-br from-primary/20 to-purple-600/20 rounded-2xl p-8 backdrop-blur-sm border">
              <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-purple-600/10 rounded-2xl"></div>
              <div className="relative space-y-4">
                {/* Natural Language Query Box */}
                <div className="bg-background/80 backdrop-blur-sm rounded-lg p-4 border">
                  <div className="flex items-center gap-2 mb-2">
                    <MessageSquare className="h-4 w-4 text-primary" />
                    <span className="text-sm font-medium">Natural Language Query</span>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    "Show me the top 5 customers by revenue this month"
                  </p>
                </div>

                {/* Generated SQL Preview */}
                <div className="bg-background/80 backdrop-blur-sm rounded-lg p-4 border">
                  <div className="flex items-center gap-2 mb-2">
                    <Database className="h-4 w-4 text-green-500" />
                    <span className="text-sm font-medium">Generated SQL</span>
                  </div>
                  <code className="text-xs font-mono text-muted-foreground">
                    SELECT customer_name, SUM(revenue) as total<br />
                    FROM orders WHERE month = CURRENT_MONTH<br />
                    GROUP BY customer_name ORDER BY total DESC LIMIT 5;
                  </code>
                </div>

                {/* Visualization */}
                <div className="bg-background/80 backdrop-blur-sm rounded-lg p-4 border">
                  <div className="flex items-center gap-2 mb-2">
                    <BarChart3 className="h-4 w-4 text-blue-500" />
                    <span className="text-sm font-medium">Visual Results</span>
                  </div>
                  <div className="space-y-2">
                    {["Acme Corp - $45,230", "TechFlow - $38,920", "DataPro - $32,150"].map((item, i) => (
                      <div key={i} className="flex items-center gap-2">
                        <div className="h-2 bg-primary rounded-full" style={{ width: `${100 - i * 20}%` }}></div>
                        <span className="text-xs text-muted-foreground">{item}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
