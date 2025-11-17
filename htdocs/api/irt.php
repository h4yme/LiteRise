<?php
/**
 * Item Response Theory (IRT) Implementation
 * Using 3-Parameter Logistic (3PL) Model
 *
 * P(θ) = c + (1-c) / (1 + e^(-a(θ-b)))
 *
 * where:
 * θ (theta) = person ability
 * a = item discrimination
 * b = item difficulty
 * c = guessing parameter
 */

class ItemResponseTheory {

    /**
     * Calculate probability of correct response using 3PL model
     *
     * @param float $theta Person ability
     * @param float $a Item discrimination
     * @param float $b Item difficulty
     * @param float $c Guessing parameter (default 0.25 for 4-choice MCQ)
     * @return float Probability between 0 and 1
     */
    public function calculateProbability($theta, $a, $b, $c = 0.25) {
        $exponent = -$a * ($theta - $b);
        $probability = $c + ((1 - $c) / (1 + exp($exponent)));
        return $probability;
    }

    /**
     * Calculate item information at a given theta
     * Higher information = more precise measurement
     *
     * @param float $theta Person ability
     * @param float $a Item discrimination
     * @param float $b Item difficulty
     * @param float $c Guessing parameter
     * @return float Information value
     */
    public function calculateInformation($theta, $a, $b, $c = 0.25) {
        $P = $this->calculateProbability($theta, $a, $b, $c);
        $Q = 1 - $P;

        // Information formula for 3PL
        $numerator = pow($a, 2) * pow(($P - $c), 2) * $Q;
        $denominator = (1 - $c) * $P;

        if ($denominator == 0) return 0;

        $information = $numerator / $denominator;
        return $information;
    }

    /**
     * Estimate ability (theta) using Maximum Likelihood Estimation
     * Based on a set of responses
     *
     * @param array $responses Array of ['isCorrect', 'a', 'b', 'c']
     * @param float $initialTheta Starting ability estimate
     * @param int $maxIterations Maximum Newton-Raphson iterations
     * @param float $tolerance Convergence threshold
     * @return float Estimated theta
     */
    public function estimateAbility($responses, $initialTheta = 0.0, $maxIterations = 50, $tolerance = 0.01) {
        // If starting from extreme values, use a more moderate starting point
        // This prevents numerical issues and ceiling/floor effects
        if ($initialTheta >= 2.5) {
            error_log("IRT: Initial theta $initialTheta is very high, using 1.5 as starting point");
            $theta = 1.5; // Start from Advanced level instead of ceiling
        } elseif ($initialTheta <= -2.5) {
            error_log("IRT: Initial theta $initialTheta is very low, using -1.5 as starting point");
            $theta = -1.5; // Start from low but not floor
        } else {
            $theta = $initialTheta;
        }

        for ($iteration = 0; $iteration < $maxIterations; $iteration++) {
            $firstDerivative = 0;
            $secondDerivative = 0;

            foreach ($responses as $response) {
                $u = $response['isCorrect'] ? 1 : 0; // 1 if correct, 0 if incorrect
                $a = $response['a'];
                $b = $response['b'];
                $c = $response['c'] ?? 0.25;

                $P = $this->calculateProbability($theta, $a, $b, $c);
                $Q = 1 - $P;

                // Prevent division by zero
                if ($P == 0 || $Q == 0) continue;

                // First derivative (slope)
                $firstDerivative += $a * (($u - $P) / ($P * $Q));

                // Second derivative (curvature)
                $numerator = (($u - $P) * (pow($P, 2) - 2*$P + 1 + pow($Q, 2)));
                $denominator = pow(($P * $Q), 2);

                if ($denominator != 0) {
                    $secondDerivative -= $a * ($numerator / $denominator);
                }
            }

            // Newton-Raphson update
            if ($secondDerivative == 0) break;

            $thetaChange = -$firstDerivative / $secondDerivative;

            // Limit step size to prevent overshooting
            $maxStepSize = 1.0;
            if (abs($thetaChange) > $maxStepSize) {
                $thetaChange = $maxStepSize * ($thetaChange > 0 ? 1 : -1);
            }

            $theta = $theta + $thetaChange;

            // Constrain theta during iteration (not just at end)
            $theta = max(-3.0, min(3.0, $theta));

            // Check convergence
            if (abs($thetaChange) < $tolerance) {
                break;
            }
        }

        // Final constraint to reasonable range (-3 to 3)
        $theta = max(-3.0, min(3.0, $theta));

        return $theta;
    }

    /**
     * Select next best item to administer
     * Uses Maximum Information criterion
     *
     * @param float $currentTheta Current ability estimate
     * @param array $availableItems Array of items with ['itemID', 'a', 'b', 'c']
     * @return array|null Selected item with maximum information
     */
    public function selectNextItem($currentTheta, $availableItems) {
        $maxInformation = -1;
        $selectedItem = null;

        foreach ($availableItems as $item) {
            $information = $this->calculateInformation(
                $currentTheta,
                $item['a'],
                $item['b'],
                $item['c'] ?? 0.25
            );

            if ($information > $maxInformation) {
                $maxInformation = $information;
                $selectedItem = $item;
            }
        }

        return $selectedItem;
    }

    /**
     * Calculate expected score at a given ability level
     *
     * @param float $theta Person ability
     * @param array $items Array of items
     * @return float Expected total score
     */
    public function calculateExpectedScore($theta, $items) {
        $expectedScore = 0;

        foreach ($items as $item) {
            $probability = $this->calculateProbability(
                $theta,
                $item['a'],
                $item['b'],
                $item['c'] ?? 0.25
            );
            $expectedScore += $probability;
        }

        return $expectedScore;
    }

    /**
     * Calculate Standard Error of Measurement
     * Indicates precision of ability estimate
     *
     * @param float $theta Person ability
     * @param array $items Array of administered items
     * @return float Standard error
     */
    public function calculateSEM($theta, $items) {
        $totalInformation = 0;

        foreach ($items as $item) {
            $information = $this->calculateInformation(
                $theta,
                $item['a'],
                $item['b'],
                $item['c'] ?? 0.25
            );
            $totalInformation += $information;
        }

        if ($totalInformation == 0) return 999; // Infinite error

        $sem = 1 / sqrt($totalInformation);
        return $sem;
    }

    /**
     * Classify ability level into categories
     *
     * @param float $theta Ability estimate
     * @return string Category (Below Basic, Basic, Proficient, Advanced)
     */
    public function classifyAbility($theta) {
        if ($theta < -1.0) {
            return "Below Basic";
        } elseif ($theta < 0.5) {
            return "Basic";
        } elseif ($theta < 1.5) {
            return "Proficient";
        } else {
            return "Advanced";
        }
    }

    /**
     * Get recommended difficulty range for next items
     *
     * @param float $theta Current ability
     * @return array ['min' => float, 'max' => float]
     */
    public function getRecommendedDifficultyRange($theta) {
        return [
            'min' => $theta - 0.5,
            'max' => $theta + 0.5
        ];
    }

    /**
     * Calculate reliability coefficient (Cronbach's Alpha approximation)
     *
     * @param array $items Array of items with IRT parameters
     * @param float $theta Ability level
     * @return float Reliability (0-1)
     */
    public function calculateReliability($items, $theta) {
        $n = count($items);
        if ($n < 2) return 0;

        $totalVariance = 0;
        $trueVariance = 0;

        foreach ($items as $item) {
            $P = $this->calculateProbability($theta, $item['a'], $item['b'], $item['c'] ?? 0.25);
            $itemVariance = $P * (1 - $P);
            $totalVariance += $itemVariance;
        }

        $sem = $this->calculateSEM($theta, $items);
        $errorVariance = pow($sem, 2);
        $trueVariance = $totalVariance - $errorVariance;

        if ($totalVariance == 0) return 0;

        $reliability = $trueVariance / $totalVariance;
        return max(0, min(1, $reliability));
    }
}

// Example usage for testing (CLI only)
if (php_sapi_name() === 'cli') {
    $irt = new ItemResponseTheory();

    echo "Testing IRT Implementation\n";
    echo "=========================\n\n";

    $theta = 0.5;
    $a = 1.5;
    $b = 0.0;
    $c = 0.25;

    $prob = $irt->calculateProbability($theta, $a, $b, $c);
    echo "Probability of correct response: " . round($prob, 4) . "\n";

    $info = $irt->calculateInformation($theta, $a, $b, $c);
    echo "Item information: " . round($info, 4) . "\n\n";

    // Test ability estimation
    $responses = [
        ['isCorrect' => true, 'a' => 1.5, 'b' => -0.5, 'c' => 0.25],
        ['isCorrect' => true, 'a' => 1.3, 'b' => 0.0, 'c' => 0.25],
        ['isCorrect' => false, 'a' => 1.8, 'b' => 1.0, 'c' => 0.25],
        ['isCorrect' => true, 'a' => 1.6, 'b' => 0.3, 'c' => 0.25],
    ];

    $estimatedTheta = $irt->estimateAbility($responses, 0.0);
    echo "Estimated ability (theta): " . round($estimatedTheta, 4) . "\n";
    echo "Ability classification: " . $irt->classifyAbility($estimatedTheta) . "\n\n";

    $sem = $irt->calculateSEM($estimatedTheta, $responses);
    echo "Standard error of measurement: " . round($sem, 4) . "\n";
}
?>
