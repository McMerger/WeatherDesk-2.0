"use client";

import { useState } from 'react';
import { Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';
import { rateForecast } from '@/app/actions';
import { cn } from '@/lib/utils';
import { Loader2 } from 'lucide-react';

type RatingProps = {
  city: string;
};

export function Rating({ city }: RatingProps) {
  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const { toast } = useToast();

  const handleRate = async () => {
    if (rating === 0) return;
    setIsSubmitting(true);
    const result = await rateForecast(rating, city);
    setIsSubmitting(false);
    setSubmitted(true);
    toast({
      title: "Rating Submitted",
      description: result.message,
    });
  };

  if (submitted) {
    return <p className="text-sm text-center text-green-600">Thanks for your feedback!</p>;
  }

  return (
    <div className="flex flex-col items-center gap-3">
      <p className="text-sm font-medium text-muted-foreground">How accurate was the forecast?</p>
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            onClick={() => setRating(star)}
            onMouseEnter={() => setHoverRating(star)}
            onMouseLeave={() => setHoverRating(0)}
            aria-label={`Rate ${star} stars`}
            className="p-1 rounded-full transition-transform hover:scale-125"
            disabled={isSubmitting}
          >
            <Star
              className={cn(
                "w-6 h-6 transition-colors",
                (hoverRating >= star || rating >= star)
                  ? "text-accent fill-accent"
                  : "text-muted-foreground/30"
              )}
            />
          </button>
        ))}
      </div>
       <Button onClick={handleRate} disabled={isSubmitting || rating === 0} size="sm" variant="outline" className="mt-1">
        {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin"/> : 'Submit Rating'}
      </Button>
    </div>
  );
}
