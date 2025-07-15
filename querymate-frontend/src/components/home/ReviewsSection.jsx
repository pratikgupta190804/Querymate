import { useEffect, useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Star } from "lucide-react"

const hardcodedReviews = [
  {
    name: "Sarah Chen",
    role: "Senior Data Analyst",
    company: "TechCorp",
    avatar: "/placeholder.svg?height=40&width=40",
    text: "QueryMate has revolutionized how our team interacts with databases. The natural language processing is incredibly accurate.",
  },
  {
    name: "Michael Rodriguez",
    role: "CTO",
    company: "StartupXYZ",
    avatar: "/placeholder.svg?height=40&width=40",
    text: "We've reduced our query writing time by 80%. The AI understands complex business logic better than expected.",
  },
  {
    name: "Emily Johnson",
    role: "Database Administrator",
    company: "Enterprise Inc",
    avatar: "/placeholder.svg?height=40&width=40",
    text: "The security features are top-notch. We can confidently use QueryMate with our most sensitive data.",
  },
  {
    name: "David Park",
    role: "Product Manager",
    company: "DataFlow",
    avatar: "/placeholder.svg?height=40&width=40",
    text: "The collaboration features have improved our team's productivity significantly. Highly recommended!",
  },
  {
    name: "Lisa Wang",
    role: "Business Intelligence Lead",
    company: "Analytics Pro",
    avatar: "/placeholder.svg?height=40&width=40",
    text: "QueryMate's visual analytics turned our boring reports into engaging dashboards. Game changer!",
  },
  {
    name: "James Thompson",
    role: "Software Engineer",
    company: "DevTools Inc",
    avatar: "/placeholder.svg?height=40&width=40",
    text: "The multi-database support is fantastic. We can query all our systems from one place.",
  },
]

const ReviewsSection = () => {
  const [currentIndex, setCurrentIndex] = useState(0)

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % hardcodedReviews.length)
    }, 4000)
    return () => clearInterval(interval)
  }, [])

  return (
    <section className="py-16 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="text-center space-y-4 mb-16">
          <Badge variant="secondary" className="w-fit mx-auto">
            Reviews
          </Badge>
          <h2 className="text-3xl sm:text-4xl font-bold">Loved by developers worldwide</h2>
          <p className="text-xl text-muted-foreground">See what our users have to say about QueryMate</p>
        </div>

        <div className="relative overflow-hidden">
          <div
            className="flex transition-transform duration-500 ease-in-out gap-6"
            style={{ transform: `translateX(-${currentIndex * (100 / 3)}%)` }}
          >
            {[...hardcodedReviews, ...hardcodedReviews].map((review, index) => (
              <Card key={index} className="flex-shrink-0 w-full md:w-1/2 lg:w-1/3 border-0 shadow-sm">
                <CardContent className="p-6 space-y-4">
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star key={star} className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                    ))}
                  </div>
                  <p className="text-muted-foreground italic">"{review.text}"</p>
                  <div className="flex items-center gap-3">
                    <Avatar className="h-10 w-10">
                      <AvatarImage src={review.avatar} />
                      <AvatarFallback>
                        {review.name
                          .split(" ")
                          .map((n) => n[0])
                          .join("")}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <p className="font-semibold text-sm">{review.name}</p>
                      <p className="text-xs text-muted-foreground">
                        {review.role} at {review.company}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}

export default ReviewsSection
