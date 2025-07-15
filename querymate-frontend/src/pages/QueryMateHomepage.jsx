"use client"

import Navbar from "../components/home/Navbar"
import HeroSection from "../components/home/HeroSection"
import FeaturesSection from "../components/home/FeaturesSection"
import ReviewsSection from "../components/home/ReviewsSection"
import Footer from "../components/home/Footer"

const QueryMateHomepage = () => {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <HeroSection />
      <FeaturesSection />
      <ReviewsSection />
      <Footer />
    </div>
  )
}

export default QueryMateHomepage
