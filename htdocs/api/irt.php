<?php
/**
 * LiteRise - Item Response Theory (IRT) Calculator
 * 3-Parameter Logistic (3PL) Model Implementation
 *
 * P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))
 *
 * Where:
 * - θ (theta) = Student ability
 * - a = Discrimination parameter
 * - b = Difficulty parameter
 * - c = Guessing parameter
 */

class IRTCalculator {

    /**
     * Calculate probability of correct response using 3PL model
     *
     * @param float $theta Student ability
     * @param float $a Discrimination parameter
     * @param float $b Difficulty parameter
     * @param float $c Guessing parameter
     * @return float Probability (0-1)
     */
    public static function calculateProbability($theta, $a, $b, $c = 0.25) {
        $exponent = -$a * ($theta - $b);
        $probability = $c + ((1 - $c) / (1 + exp($exponent)));
        return $probability;
    }

    /**
     * Calculate information function I(θ) for an item
     * Measures how much information an item provides at ability level θ
     *
     * @param float $theta Student ability
     * @param float $a Discrimination parameter
     * @param float $b Difficulty parameter
     * @param float $c Guessing parameter
     * @return float Information value
     */
    public static function itemInformation($theta, $a, $b, $c = 0.25) {
        $p = self::calculateProbability($theta, $a, $b, $c);
        $q = 1 - $p;

        // I(θ) = a² * Q(θ)² / P(θ)
        // Where Q(θ) = (P(θ) - c) / (1 - c)
        $qTheta = ($p - $c) / (1 - $c);
        $information = ($a * $a * $qTheta * $qTheta) / $p;

        return $information;
    }

    /**
     * Update student ability using Maximum Likelihood Estimation (MLE)
     * Uses Newton-Raphson method for theta estimation
     *
     * @param float $currentTheta Current ability estimate
     * @param array $responses Array of student responses
     * @param array $items Array of item parameters
     * @param int $maxIterations Maximum iterations
     * @param float $tolerance Convergence tolerance
     * @return float Updated theta
     */
    public static function updateAbility($currentTheta, $responses, $items, $maxIterations = 20, $tolerance = 0.001) {
        $theta = $currentTheta;

        for ($iter = 0; $iter < $maxIterations; $iter++) {
            $firstDerivative = 0;  // L'(θ)
            $secondDerivative = 0; // L''(θ)

            foreach ($responses as $index => $response) {
                $item = $items[$index];
                $a = $item['Discrimination'];
                $b = $item['Difficulty'];
                $c = isset($item['Guessing']) ? $item['Guessing'] : 0.25;
                $u = $response['Correct'] ? 1 : 0; // 1 if correct, 0 if incorrect

                $p = self::calculateProbability($theta, $a, $b, $c);
                $q = 1 - $p;

                // First derivative components
                $pPrime = $a * $p * $q / (1 - $c);
                $firstDerivative += ($u - $p) * $pPrime / ($p * $q);

                // Second derivative components
                $pDoublePrime = $a * $a * $p * $q * ($q - $p) / (($1 - $c) * (1 - $c));
                $secondDerivative += ($pDoublePrime * ($u - $p) - $pPrime * $pPrime * (1 - 2 * $p)) / ($p * $q);
            }

            // Newton-Raphson update
            if ($secondDerivative == 0) {
                break;
            }

            $thetaNew = $theta - ($firstDerivative / $secondDerivative);

            // Check convergence
            if (abs($thetaNew - $theta) < $tolerance) {
                $theta = $thetaNew;
                break;
            }

            $theta = $thetaNew;

            // Constrain theta to reasonable range (-4 to 4)
            $theta = max(-4, min(4, $theta));
        }

        return round($theta, 4);
    }

    /**
     * Estimate ability using Expected A Posteriori (EAP) method
     * Alternative to MLE, more stable for extreme scores
     *
     * @param array $responses Student responses
     * @param array $items Item parameters
     * @param float $priorMean Prior distribution mean (default 0)
     * @param float $priorSD Prior distribution SD (default 1)
     * @return float Estimated theta
     */
    public static function estimateAbilityEAP($responses, $items, $priorMean = 0, $priorSD = 1) {
        // Quadrature points for numerical integration
        $quadPoints = self::getQuadraturePoints();

        $numerator = 0;
        $denominator = 0;

        foreach ($quadPoints as $point) {
            $theta = $point['x'];
            $weight = $point['weight'];

            // Calculate likelihood
            $likelihood = 1;
            foreach ($responses as $index => $response) {
                $item = $items[$index];
                $a = $item['Discrimination'];
                $b = $item['Difficulty'];
                $c = isset($item['Guessing']) ? $item['Guessing'] : 0.25;
                $u = $response['Correct'] ? 1 : 0;

                $p = self::calculateProbability($theta, $a, $b, $c);
                $likelihood *= $u ? $p : (1 - $p);
            }

            // Apply prior (normal distribution)
            $prior = self::normalPDF($theta, $priorMean, $priorSD);

            $posterior = $likelihood * $prior * $weight;
            $numerator += $theta * $posterior;
            $denominator += $posterior;
        }

        return $denominator > 0 ? round($numerator / $denominator, 4) : $priorMean;
    }

    /**
     * Select next best item using Maximum Information criterion
     *
     * @param float $theta Current ability estimate
     * @param array $availableItems Pool of unused items
     * @return array|null Best item to administer
     */
    public static function selectNextItem($theta, $availableItems) {
        if (empty($availableItems)) {
            return null;
        }

        $maxInformation = -1;
        $bestItem = null;

        foreach ($availableItems as $item) {
            $a = $item['Discrimination'];
            $b = $item['Difficulty'];
            $c = isset($item['Guessing']) ? $item['Guessing'] : 0.25;

            $information = self::itemInformation($theta, $a, $b, $c);

            if ($information > $maxInformation) {
                $maxInformation = $information;
                $bestItem = $item;
            }
        }

        return $bestItem;
    }

    /**
     * Calculate standard error of ability estimate
     *
     * @param float $theta Ability estimate
     * @param array $items Administered items
     * @return float Standard error
     */
    public static function calculateStandardError($theta, $items) {
        $totalInformation = 0;

        foreach ($items as $item) {
            $a = $item['Discrimination'];
            $b = $item['Difficulty'];
            $c = isset($item['Guessing']) ? $item['Guessing'] : 0.25;

            $totalInformation += self::itemInformation($theta, $a, $b, $c);
        }

        return $totalInformation > 0 ? round(1 / sqrt($totalInformation), 4) : 9999;
    }

    /**
     * Get quadrature points for EAP estimation
     * Returns Gauss-Hermite quadrature points
     *
     * @return array Quadrature points with weights
     */
    private static function getQuadraturePoints() {
        // 15-point Gauss-Hermite quadrature (scaled for theta range -4 to 4)
        return [
            ['x' => -3.5, 'weight' => 0.0001],
            ['x' => -3.0, 'weight' => 0.0017],
            ['x' => -2.5, 'weight' => 0.0137],
            ['x' => -2.0, 'weight' => 0.0540],
            ['x' => -1.5, 'weight' => 0.1209],
            ['x' => -1.0, 'weight' => 0.1747],
            ['x' => -0.5, 'weight' => 0.1974],
            ['x' =>  0.0, 'weight' => 0.2066],
            ['x' =>  0.5, 'weight' => 0.1974],
            ['x' =>  1.0, 'weight' => 0.1747],
            ['x' =>  1.5, 'weight' => 0.1209],
            ['x' =>  2.0, 'weight' => 0.0540],
            ['x' =>  2.5, 'weight' => 0.0137],
            ['x' =>  3.0, 'weight' => 0.0017],
            ['x' =>  3.5, 'weight' => 0.0001]
        ];
    }

    /**
     * Normal probability density function
     *
     * @param float $x Value
     * @param float $mean Mean
     * @param float $sd Standard deviation
     * @return float PDF value
     */
    private static function normalPDF($x, $mean = 0, $sd = 1) {
        $variance = $sd * $sd;
        $coefficient = 1 / sqrt(2 * M_PI * $variance);
        $exponent = -pow($x - $mean, 2) / (2 * $variance);
        return $coefficient * exp($exponent);
    }

    /**
     * Calculate test reliability (Cronbach's Alpha equivalent for IRT)
     *
     * @param array $items Administered items
     * @param float $theta Ability estimate
     * @return float Reliability coefficient
     */
    public static function calculateReliability($items, $theta) {
        $totalInformation = 0;

        foreach ($items as $item) {
            $a = $item['Discrimination'];
            $b = $item['Difficulty'];
            $c = isset($item['Guessing']) ? $item['Guessing'] : 0.25;
            $totalInformation += self::itemInformation($theta, $a, $b, $c);
        }

        $reliability = $totalInformation / (1 + $totalInformation);
        return round($reliability, 4);
    }
}
