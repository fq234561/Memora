import {
  PromptOptimizeRequest,
  OptimizedPromptResult,
  ModelParams,
} from '../models/prompt';

// Activity descriptors mapped to family memory language
const activitySubjectMap: Record<string, string> = {
  travel:
    'a natural family group photo at a scenic travel destination, everyone casually posed or candid, with the absent person naturally blended into the scene, preserving the original trip atmosphere',
  party:
    'a lively party or celebration gathering, friends and family mingling naturally, the absent person seamlessly part of the group, festive lighting and genuine smiles',
  holiday:
    'a warm holiday family gathering around a decorated table or living room, cozy seasonal ambiance, the absent person naturally included in the group composition',
  birthday:
    'a joyful birthday celebration with cake, balloons, and family around, the absent person naturally present in the group, candles and warm ambient light',
  wedding:
    'an elegant wedding or ceremony moment, family and guests in formal or semi-formal attire, the absent person naturally positioned among loved ones, romantic soft lighting',
  graduation:
    'a proud graduation day family photo outdoors or in a hall, caps and gowns or smart casual wear, the absent person naturally standing with the graduate and family',
  reunion:
    'a heartfelt family reunion group photo, multi-generational gathering in a home or garden, everyone close together, the absent person naturally embraced in the group',
  daily:
    'an everyday candid family moment at home or a familiar place, casual relaxed postures, natural indoor light, the absent person naturally part of the domestic scene',
  other:
    'a natural group family photo capturing a meaningful shared moment, everyone at ease, the absent person naturally integrated with gentle warmth',
};

// Photo style composition templates
const styleCompositionMap: Record<string, string> = {
  natural_family:
    'medium shot, subjects centered or slightly off-center using rule of thirds, soft environmental background with gentle bokeh, natural candid posture rather than stiff posing, warm group dynamic',
  travel_memory:
    'medium to wide shot, subjects framed against a scenic travel backdrop, natural daylight or golden hour light, relaxed casual poses, sense of adventure and togetherness',
  party_gathering:
    'medium shot, dynamic group composition with festive background elements, colorful ambient lighting, genuine expressions of laughter and connection, depth from decorations',
  holiday_celebration:
    'medium shot, warm indoor setting with seasonal decor subtly visible, soft ambient glow from lights or fireplace, intimate family circle composition, cozy atmosphere',
  milestone_event:
    'medium to wide shot, dignified composition befitting the occasion, formal or smart attire, proud and joyful posture, elegant backdrop with soft focus',
};

// Style-specific color and texture direction
const styleDirectionMap: Record<string, string> = {
  NATURAL_FAMILY:
    'natural color grading with warm skin tones, soft matte finish, lifelike texture, minimal retouching, authentic family warmth',
  TRAVEL_MEMORY:
    'vibrant yet natural travel palette, sun-kissed tones, slight depth from landscape, crisp detail with warm highlights',
  PARTY_GATHERING:
    'cheerful warm palette with soft bokeh highlights from lights, gentle contrast, lively but not oversaturated, festive warmth',
  HOLIDAY_CELEBRATION:
    'rich warm seasonal tones, soft ambient glow, cozy contrast, gentle vignette of togetherness, timeless family warmth',
  MILESTONE_EVENT:
    'elegant rich color grading, refined contrast, formal portrait lighting with group depth, timeless quality of a special day',
};

// Mood modifiers
const moodModifierMap: Record<string, string> = {
  warm: 'warm, comforting atmosphere with golden ambient light, joyful gathering energy',
  nostalgic: 'soft, dreamlike nostalgia with gentle haze and muted highlights, cozy reunion feeling',
  peaceful: 'serene, tranquil mood with stillness and quiet contentment, gentle family calm',
  celebratory: 'joyful celebration, bright happy energy mixed with cozy togetherness',
  solemn: 'dignified, heartfelt atmosphere with subdued tones and respectful family closeness',
};

// Composition preference overrides
const compositionOverrideMap: Record<string, string> = {
  side_by_side: 'subjects standing shoulder to shoulder, facing slightly toward each other with relaxed posture',
  embrace: 'a gentle, natural group embrace or arms around shoulders, conveying love without dramatic gesture',
  seated_together: 'subjects seated closely on a sofa or bench, turned slightly toward one another in comfortable intimacy',
  holding_hands: 'subjects standing or walking with hands gently clasped, expressing quiet connection',
  looking_at_each_other: 'subjects facing one another with soft eye contact, expressions of fond recognition and warmth',
};

// Fixed family safety constraints injected into every prompt
const familySafetyConstraints = [
  'This is a private family memory image, not a historical photograph.',
  'AI-generated family photo. Natural and respectful tone only.',
  'Do not depict speech, movement, animation, or resurrection.',
  'Do not include text artifacts, watermarks, signatures, or commercial branding.',
  'Preserve the facial identity and natural appearance of the subjects from the provided reference photos.',
  'Do not impersonate public figures or create deceptive content.',
];

// Fixed negative prompt (exclusion list)
const fixedNegativePrompt =
  'sensual, revealing clothing, commercial advertisement, brand logos, text overlays, cartoonish, anime, manga, distorted facial features, extra limbs, unnatural skin texture, plastic skin, gore, horror, laughing exaggeratedly, political imagery, crowded background, harsh flash photography, oversaturated colors, low quality, blurry, deformed hands';

// Model params recommendation
const defaultModelParams: ModelParams = {
  size: '1024x1536',
  quality: 'high',
  style: 'natural',
};

export class PromptOptimizer {
  build(request: PromptOptimizeRequest): OptimizedPromptResult {
    const {
      userDescription,
      activityType,
      personTypes,
      eventContext,
      style,
      mood,
      compositionPrefs,
    } = request;

    // 1. Subject
    const subjectBase =
      activitySubjectMap[activityType?.toLowerCase() || 'other'] ||
      activitySubjectMap['other'];
    const subject = userDescription
      ? `${subjectBase}. Additional context from the user: ${userDescription}`
      : subjectBase;

    // 2. Composition
    const styleKey = style?.toLowerCase() || 'natural_family';
    const compositionBase =
      styleCompositionMap[styleKey] ||
      styleCompositionMap['natural_family'];
    const composition = compositionPrefs
      ? `${compositionOverrideMap[compositionPrefs.toLowerCase()] || compositionBase}. Overall framing: ${compositionBase}`
      : compositionBase;

    // 3. Style direction
    const styleDirection =
      styleDirectionMap[style] ||
      styleDirectionMap['NATURAL_FAMILY'];

    // 4. Mood
    const moodText = mood
      ? moodModifierMap[mood.toLowerCase()] || `a ${mood} atmosphere`
      : 'a warm, joyful atmosphere of family togetherness';

    // 5. Lighting & technicals
    const lightingTechnicals =
      'Soft diffused natural light or warm window light. ' +
      '85mm portrait lens look at f/2.0, shallow depth of field with creamy bokeh. ' +
      'Hyper-realistic skin texture, individual hair strands visible, natural pores and soft shadows. ' +
      'Slight film grain for emotional warmth. ' +
      'Vertical portrait orientation (3:4 aspect ratio), medium close-up to medium shot framing.';

    // 6. Safety constraints paragraph
    const safetyParagraph = familySafetyConstraints.join(' ');

    // Assemble optimized prompt
    const optimizedPrompt = [
      `Subject: ${subject}.`,
      `Composition: ${composition}.`,
      `Style & Color: ${styleDirection}.`,
      `Mood: ${moodText}.`,
      `Lighting & Technicals: ${lightingTechnicals}`,
      `Safety & Context: ${safetyParagraph}`,
    ].join('\n\n');

    // Style keywords (shorter tag-line version)
    const styleKeyword = style.toLowerCase().replace(/_/g, ' ');
    const activityKeyword = (activityType || 'family').toLowerCase().replace(/_/g, ' ');
    const stylePrompt = [
      'family memory photo',
      'photorealistic',
      'natural',
      'warm',
      'natural lighting',
      'shallow depth of field',
      ...(styleKeyword !== activityKeyword ? [styleKeyword, activityKeyword] : [styleKeyword]),
      'natural group portrait',
    ].join(', ');

    return {
      optimizedPrompt,
      negativePrompt: fixedNegativePrompt,
      stylePrompt,
      safetyNotes: [...familySafetyConstraints],
      modelParams: { ...defaultModelParams },
    };
  }
}

export const promptOptimizer = new PromptOptimizer();
