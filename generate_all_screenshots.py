import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

WIDTH = 1080
HEIGHT = 2400

FONT_HEAD = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 56)
FONT_SUB = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 28)
FONT_PHONE_TITLE = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 38)
FONT_PHONE_SUB = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 24)
FONT_TILE = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 54)
FONT_KEY = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 34)
FONT_KEY_SPEC = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 24)
FONT_CALLOUT = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 25)
FONT_CARD_TITLE = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 28)
FONT_BODY = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 24)
FONT_BODY_BOLD = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 26)
FONT_BODY_SMALL = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 20)

COLOR_GREEN_FACE = (46, 125, 50)
COLOR_GREEN_LIP  = (27, 94, 32)
COLOR_GREEN_RIM  = (102, 187, 106)

COLOR_ORANGE_FACE = (245, 124, 0)
COLOR_ORANGE_LIP  = (230, 81, 0)
COLOR_ORANGE_RIM  = (255, 183, 77)

COLOR_SLATE_FACE = (51, 65, 85)
COLOR_SLATE_LIP  = (30, 41, 59)
COLOR_SLATE_RIM  = (100, 116, 139)

def create_base_canvas(headline, subtitle, ambient_color=(46, 125, 50, 45)):
    img = Image.new("RGBA", (WIDTH, HEIGHT), (11, 15, 23, 255))
    
    # Ambient top glow
    glow = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gdraw.ellipse([WIDTH//2 - 450, -120, WIDTH//2 + 450, 480], fill=ambient_color)
    glow = glow.filter(ImageFilter.GaussianBlur(radius=110))
    img.paste(Image.alpha_composite(Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0)), glow), (0, 0), glow)
    
    draw = ImageDraw.Draw(img)
    
    # Marketing Headline & Subtitle
    draw.text((WIDTH // 2, 140), headline, font=FONT_HEAD, fill=(255, 255, 255, 255), anchor="mm")
    draw.text((WIDTH // 2, 215), subtitle, font=FONT_SUB, fill=(148, 163, 184, 255), anchor="mm")
    
    # Phone Body Bezel (Mockup Frame)
    frame_x1, frame_y1 = 90, 280
    frame_x2, frame_y2 = WIDTH - 90, HEIGHT - 160
    
    # Drop shadow for device frame
    shadow = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.rounded_rectangle([frame_x1 - 10, frame_y1 - 5, frame_x2 + 10, frame_y2 + 25], radius=68, fill=(0, 0, 0, 180))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=25))
    img.paste(Image.alpha_composite(Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0)), shadow), (0, 0), shadow)
    
    draw = ImageDraw.Draw(img)
    # Outer titanium bezel
    draw.rounded_rectangle([frame_x1, frame_y1, frame_x2, frame_y2], radius=60, fill=(15, 20, 28, 255), outline=(45, 55, 72, 255), width=6)
    
    # Inner glass screen
    screen_x1, screen_y1 = frame_x1 + 16, frame_y1 + 16
    screen_x2, screen_y2 = frame_x2 - 16, frame_y2 - 16
    draw.rounded_rectangle([screen_x1, screen_y1, screen_x2, screen_y2], radius=48, fill=(11, 15, 23, 255))
    
    return img, draw, (screen_x1, screen_y1, screen_x2, screen_y2)

def draw_keyboard(draw, x_left, y_bottom, width, key_states=None):
    if key_states is None:
        key_states = {
            "A": ((46, 125, 50), (255, 255, 255)),
            "E": ((46, 125, 50), (255, 255, 255)),
            "R": ((46, 125, 50), (255, 255, 255)),
            "T": ((51, 65, 85), (255, 255, 255)),
            "I": ((245, 124, 0), (255, 255, 255)),
            "O": ((245, 124, 0), (255, 255, 255)),
            "S": ((245, 124, 0), (255, 255, 255)),
            "L": ((245, 124, 0), (255, 255, 255)),
            "P": ((51, 65, 85), (255, 255, 255)),
        }
    rows = [
        ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"],
        ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
        ["ENT", "Z", "X", "C", "V", "B", "N", "M", "DEL"]
    ]
    key_h = 76
    gap = 8
    key_w = (width - gap * 9) // 10
    start_y = y_bottom - (key_h * 3 + gap * 2)
    
    for r_idx, row in enumerate(rows):
        row_y = start_y + r_idx * (key_h + gap)
        if r_idx == 0:
            row_x = x_left
            for k in row:
                col, text_col = key_states.get(k, ((36, 46, 61), (255, 255, 255)))
                draw.rounded_rectangle([row_x, row_y, row_x + key_w, row_y + key_h], radius=10, fill=col)
                draw.text((row_x + key_w // 2, row_y + key_h // 2), k, font=FONT_KEY, fill=text_col, anchor="mm")
                row_x += key_w + gap
        elif r_idx == 1:
            row_w_total = len(row) * key_w + (len(row) - 1) * gap
            row_x = x_left + (width - row_w_total) // 2
            for k in row:
                col, text_col = key_states.get(k, ((36, 46, 61), (255, 255, 255)))
                draw.rounded_rectangle([row_x, row_y, row_x + key_w, row_y + key_h], radius=10, fill=col)
                draw.text((row_x + key_w // 2, row_y + key_h // 2), k, font=FONT_KEY, fill=text_col, anchor="mm")
                row_x += key_w + gap
        else:
            spec_w = int(key_w * 1.5)
            row_w_total = spec_w * 2 + 7 * key_w + 8 * gap
            row_x = x_left + (width - row_w_total) // 2
            for k in row:
                this_w = spec_w if k in ("ENT", "DEL") else key_w
                col, text_col = key_states.get(k, ((36, 46, 61), (255, 255, 255)))
                draw.rounded_rectangle([row_x, row_y, row_x + this_w, row_y + key_h], radius=10, fill=col)
                f = FONT_KEY_SPEC if k in ("ENT", "DEL") else FONT_KEY
                draw.text((row_x + this_w // 2, row_y + key_h // 2), k, font=f, fill=text_col, anchor="mm")
                row_x += this_w + gap

# 1. Classic Daily
def make_screenshot_1():
    img, draw, screen = create_base_canvas(
        headline="TACTILE 2.5D TILES",
        subtitle="Mechanical Keycaps & 3D Air-Lift Flip"
    )
    sx1, sy1, sx2, sy2 = screen
    margin = 36
    content_w = (sx2 - sx1) - margin * 2
    
    draw.text((sx1 + margin, sy1 + 50), "NenoKisia", font=FONT_PHONE_TITLE, fill=(255, 255, 255, 255))
    draw.text((sx2 - margin, sy1 + 54), "DAILY #842", font=FONT_PHONE_SUB, fill=(74, 222, 128, 255), anchor="ra")
    
    grid_data = [
        [("P", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("L", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP), ("A", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("T", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("E", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP)],
        [("S", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("L", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("A", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("T", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("E", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [("S", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("L", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("A", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("R", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("E", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [("S", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("L", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("A", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("M", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("E", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [("S", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("L", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("A", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("Z", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("E", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [(None, None, None)] * 5
    ]
    
    tile_size = 110
    gap = 14
    start_grid_x = sx1 + (content_w - (tile_size * 5 + gap * 4)) // 2 + margin
    start_grid_y = sy1 + 130
    
    for r_idx, row in enumerate(grid_data):
        for c_idx, (letter, face, lip) in enumerate(row):
            tx = start_grid_x + c_idx * (tile_size + gap)
            ty = start_grid_y + r_idx * (tile_size + gap)
            if letter is None:
                draw.rounded_rectangle([tx, ty, tx + tile_size, ty + tile_size], radius=16, outline=(30, 41, 59, 140), width=3)
            else:
                draw.rounded_rectangle([tx, ty + 10, tx + tile_size, ty + tile_size + 10], radius=18, fill=lip)
                draw.rounded_rectangle([tx, ty, tx + tile_size, ty + tile_size], radius=18, fill=face)
                draw.text((tx + tile_size // 2, ty + tile_size // 2), letter, font=FONT_TILE, fill=(255, 255, 255, 255), anchor="mm")

    callout_y = start_grid_y + 6 * (tile_size + gap) + 30
    draw.rounded_rectangle([sx1 + margin, callout_y, sx2 - margin, callout_y + 66], radius=16, fill=(30, 41, 59, 140))
    draw.text((WIDTH // 2, callout_y + 33), "3D Air-Lift Flip & Colored Ambient Drop Shadows", font=FONT_CALLOUT, fill=(203, 213, 225, 255), anchor="mm")
    draw_keyboard(draw, sx1 + margin, sy2 - 40, content_w)
    img.save("play_store_assets/screenshot_01_classic_daily.png", "PNG", optimize=True)
    print("Screenshot 1 saved.")

# 2. Multi-Board
def make_screenshot_2():
    img, draw, screen = create_base_canvas(
        headline="MULTI-BOARD ACTION",
        subtitle="Solve Duo & Quad Boards Simultaneously",
        ambient_color=(30, 58, 138, 45)
    )
    sx1, sy1, sx2, sy2 = screen
    margin = 36
    content_w = (sx2 - sx1) - margin * 2
    
    draw.text((sx1 + margin, sy1 + 50), "Quad Board Mode", font=FONT_PHONE_TITLE, fill=(255, 255, 255, 255))
    draw.text((sx2 - margin, sy1 + 54), "4 BOARDS • 9 TRIES", font=FONT_PHONE_SUB, fill=(245, 158, 11, 255), anchor="ra")
    
    card_w = (content_w - 20) // 2
    card_h = 360
    card_gap = 20
    
    boards = [
        ("BOARD 1 - SOLVED", (74, 222, 128), "CRANE"),
        ("BOARD 2 - SOLVED", (74, 222, 128), "PILOT"),
        ("BOARD 3 - ACTIVE", (245, 158, 11), "FLOCK"),
        ("BOARD 4 - ACTIVE", (245, 158, 11), "GHOST"),
    ]
    
    for idx, (title, color, word) in enumerate(boards):
        col = idx % 2
        row = idx // 2
        bx = sx1 + margin + col * (card_w + card_gap)
        by = sy1 + 130 + row * (card_h + card_gap)
        
        draw.rounded_rectangle([bx, by, bx + card_w, by + card_h], radius=20, fill=(15, 23, 42, 200), outline=(51, 65, 85, 255), width=2)
        draw.text((bx + 16, by + 18), title, font=FONT_BODY_BOLD, fill=color)
        
        mtile_size = 46
        mgap = 8
        m_start_x = bx + (card_w - (mtile_size * 5 + mgap * 4)) // 2
        m_start_y = by + 65
        
        for mr in range(4):
            for mc in range(5):
                tx = m_start_x + mc * (mtile_size + mgap)
                ty = m_start_y + mr * (mtile_size + mgap)
                if mr == 3 and "SOLVED" in title:
                    draw.rounded_rectangle([tx, ty, tx + mtile_size, ty + mtile_size], radius=8, fill=COLOR_GREEN_FACE)
                    draw.text((tx + mtile_size // 2, ty + mtile_size // 2), word[mc], font=FONT_KEY, fill=(255, 255, 255), anchor="mm")
                elif mr == 3:
                    c = COLOR_GREEN_FACE if mc in (0, 4) else COLOR_ORANGE_FACE
                    draw.rounded_rectangle([tx, ty, tx + mtile_size, ty + mtile_size], radius=8, fill=c)
                    draw.text((tx + mtile_size // 2, ty + mtile_size // 2), word[mc], font=FONT_KEY, fill=(255, 255, 255), anchor="mm")
                else:
                    draw.rounded_rectangle([tx, ty, tx + mtile_size, ty + mtile_size], radius=8, fill=(30, 41, 59, 140))
                    
    callout_y = sy1 + 130 + 2 * (card_h + card_gap) + 20
    draw.rounded_rectangle([sx1 + margin, callout_y, sx2 - margin, callout_y + 66], radius=16, fill=(30, 41, 59, 140))
    draw.text((WIDTH // 2, callout_y + 33), "Quad-Split Keyboard Tracks All 4 Boards At Once", font=FONT_CALLOUT, fill=(203, 213, 225, 255), anchor="mm")
    draw_keyboard(draw, sx1 + margin, sy2 - 40, content_w)
    img.save("play_store_assets/screenshot_02_multiboard.png", "PNG", optimize=True)
    print("Screenshot 2 saved.")

# 3. Campaign Boss Battle
def make_screenshot_3():
    img, draw, screen = create_base_canvas(
        headline="50-LEVEL RPG CAMPAIGN",
        subtitle="4 Worlds, 3-Star Ratings & Real-Time Boss Battles",
        ambient_color=(220, 38, 38, 40)
    )
    sx1, sy1, sx2, sy2 = screen
    margin = 36
    content_w = (sx2 - sx1) - margin * 2
    
    draw.text((sx1 + margin, sy1 + 50), "World 1: Emerald Grove", font=FONT_PHONE_TITLE, fill=(255, 255, 255, 255))
    draw.text((sx2 - margin, sy1 + 54), "LEVEL 10: BOSS", font=FONT_PHONE_SUB, fill=(239, 68, 68, 255), anchor="ra")
    
    boss_y = sy1 + 120
    draw.rounded_rectangle([sx1 + margin, boss_y, sx2 - margin, boss_y + 140], radius=22, fill=(15, 23, 42, 220), outline=(239, 68, 68, 160), width=2)
    draw.text((sx1 + margin + 24, boss_y + 26), "BOSS: Shadow Dragon", font=FONT_CARD_TITLE, fill=(255, 255, 255))
    draw.text((sx2 - margin - 28, boss_y + 28), "HP: 35 / 100", font=FONT_BODY_BOLD, fill=(239, 68, 68), anchor="ra")
    
    hbar_x = sx1 + margin + 24
    hbar_w = content_w - 48
    draw.rounded_rectangle([hbar_x, boss_y + 75, hbar_x + hbar_w, boss_y + 105], radius=15, fill=(30, 41, 59))
    draw.rounded_rectangle([hbar_x, boss_y + 75, hbar_x + int(hbar_w * 0.35), boss_y + 105], radius=15, fill=(239, 68, 68))
    
    grid_data = [
        [("S", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("P", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP), ("A", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("R", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("K", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP)],
        [("F", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("L", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP), ("A", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("M", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("E", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [("P", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("O", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("W", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("E", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("R", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [(None, None, None)] * 5
    ]
    tile_size = 110
    gap = 14
    start_grid_x = sx1 + (content_w - (tile_size * 5 + gap * 4)) // 2 + margin
    start_grid_y = boss_y + 175
    
    for r_idx, row in enumerate(grid_data):
        for c_idx, (letter, face, lip) in enumerate(row):
            tx = start_grid_x + c_idx * (tile_size + gap)
            ty = start_grid_y + r_idx * (tile_size + gap)
            if letter is None:
                draw.rounded_rectangle([tx, ty, tx + tile_size, ty + tile_size], radius=16, outline=(30, 41, 59, 140), width=3)
            else:
                draw.rounded_rectangle([tx, ty + 10, tx + tile_size, ty + tile_size + 10], radius=18, fill=lip)
                draw.rounded_rectangle([tx, ty, tx + tile_size, ty + tile_size], radius=18, fill=face)
                draw.text((tx + tile_size // 2, ty + tile_size // 2), letter, font=FONT_TILE, fill=(255, 255, 255), anchor="mm")

    callout_y = start_grid_y + 4 * (tile_size + gap) + 40
    draw.rounded_rectangle([sx1 + margin, callout_y, sx2 - margin, callout_y + 66], radius=16, fill=(30, 41, 59, 140))
    draw.text((WIDTH // 2, callout_y + 33), "Earn 3 Stars & Defeat Vocabulary Bosses", font=FONT_CALLOUT, fill=(203, 213, 225, 255), anchor="mm")
    draw_keyboard(draw, sx1 + margin, sy2 - 40, content_w)
    img.save("play_store_assets/screenshot_03_campaign_boss.png", "PNG", optimize=True)
    print("Screenshot 3 saved.")

# 4. Timed Blitz
def make_screenshot_4():
    img, draw, screen = create_base_canvas(
        headline="TIMED RUSH BLITZ",
        subtitle="120-Second High-Speed Deductive Sprint",
        ambient_color=(245, 124, 0, 40)
    )
    sx1, sy1, sx2, sy2 = screen
    margin = 36
    content_w = (sx2 - sx1) - margin * 2
    
    draw.text((sx1 + margin, sy1 + 50), "Timed Rush", font=FONT_PHONE_TITLE, fill=(255, 255, 255, 255))
    draw.text((sx2 - margin, sy1 + 54), "TIME: 01:24", font=FONT_PHONE_SUB, fill=(251, 146, 60, 255), anchor="ra")
    
    score_y = sy1 + 120
    draw.rounded_rectangle([sx1 + margin, score_y, sx2 - margin, score_y + 130], radius=22, fill=(15, 23, 42, 220), outline=(245, 124, 0, 160), width=2)
    draw.text((sx1 + margin + 24, score_y + 35), "SCORE: 4,850 pts", font=FONT_CARD_TITLE, fill=(255, 255, 255))
    draw.text((sx2 - margin - 28, score_y + 35), "COMBO: 3X MULTIPLIER", font=FONT_CARD_TITLE, fill=(251, 146, 60), anchor="ra")
    
    cbar_x = sx1 + margin + 24
    cbar_w = content_w - 48
    draw.rounded_rectangle([cbar_x, score_y + 80, cbar_x + cbar_w, score_y + 104], radius=12, fill=(30, 41, 59))
    draw.rounded_rectangle([cbar_x, score_y + 80, cbar_x + int(cbar_w * 0.75), score_y + 104], radius=12, fill=(245, 124, 0))
    
    grid_data = [
        [("S", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("W", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("I", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP), ("F", COLOR_SLATE_FACE, COLOR_SLATE_LIP), ("T", COLOR_ORANGE_FACE, COLOR_ORANGE_LIP)],
        [("B", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("L", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("I", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("T", COLOR_GREEN_FACE, COLOR_GREEN_LIP), ("Z", COLOR_GREEN_FACE, COLOR_GREEN_LIP)],
        [(None, None, None)] * 5,
        [(None, None, None)] * 5
    ]
    tile_size = 110
    gap = 14
    start_grid_x = sx1 + (content_w - (tile_size * 5 + gap * 4)) // 2 + margin
    start_grid_y = score_y + 175
    
    for r_idx, row in enumerate(grid_data):
        for c_idx, (letter, face, lip) in enumerate(row):
            tx = start_grid_x + c_idx * (tile_size + gap)
            ty = start_grid_y + r_idx * (tile_size + gap)
            if letter is None:
                draw.rounded_rectangle([tx, ty, tx + tile_size, ty + tile_size], radius=16, outline=(30, 41, 59, 140), width=3)
            else:
                draw.rounded_rectangle([tx, ty + 10, tx + tile_size, ty + tile_size + 10], radius=18, fill=lip)
                draw.rounded_rectangle([tx, ty, tx + tile_size, ty + tile_size], radius=18, fill=face)
                draw.text((tx + tile_size // 2, ty + tile_size // 2), letter, font=FONT_TILE, fill=(255, 255, 255), anchor="mm")

    callout_y = start_grid_y + 4 * (tile_size + gap) + 40
    draw.rounded_rectangle([sx1 + margin, callout_y, sx2 - margin, callout_y + 66], radius=16, fill=(30, 41, 59, 140))
    draw.text((WIDTH // 2, callout_y + 33), "Chain Rapid Solves Before the Clock Runs Out", font=FONT_CALLOUT, fill=(203, 213, 225, 255), anchor="mm")
    draw_keyboard(draw, sx1 + margin, sy2 - 40, content_w)
    img.save("play_store_assets/screenshot_04_timed_blitz.png", "PNG", optimize=True)
    print("Screenshot 4 saved.")

# 5. Bot Analysis & Stats
def make_screenshot_5():
    img, draw, screen = create_base_canvas(
        headline="SMART BOT ANALYSIS",
        subtitle="Information Entropy & 28-Day Streak Archive",
        ambient_color=(34, 197, 94, 40)
    )
    sx1, sy1, sx2, sy2 = screen
    margin = 36
    content_w = (sx2 - sx1) - margin * 2
    
    draw.text((sx1 + margin, sy1 + 50), "NenoKisia Bot Analysis", font=FONT_PHONE_TITLE, fill=(255, 255, 255, 255))
    draw.text((sx2 - margin, sy1 + 54), "SKILL: 98/100", font=FONT_PHONE_SUB, fill=(74, 222, 128, 255), anchor="ra")
    
    card_y = sy1 + 120
    draw.rounded_rectangle([sx1 + margin, card_y, sx2 - margin, card_y + 360], radius=22, fill=(15, 23, 42, 220), outline=(59, 130, 246, 160), width=2)
    draw.text((sx1 + margin + 24, card_y + 24), "GUESS EFFICIENCY BREAKDOWN", font=FONT_BODY_BOLD, fill=(96, 165, 250))
    
    steps = [
        ("1. CRANE", "Candidates: 2,315 → 42 (98.2% narrowed)"),
        ("2. SLOTH", "Candidates: 42 → 3 (92.8% narrowed)"),
        ("3. GHOST", "Solved! Optimal information extraction")
    ]
    sy = card_y + 75
    for title, desc in steps:
        draw.text((sx1 + margin + 24, sy), title, font=FONT_BODY_BOLD, fill=(255, 255, 255))
        draw.text((sx1 + margin + 24, sy + 34), desc, font=FONT_BODY, fill=(148, 163, 184))
        sy += 85
        
    cal_y = card_y + 400
    draw.rounded_rectangle([sx1 + margin, cal_y, sx2 - margin, cal_y + 540], radius=22, fill=(15, 23, 42, 220), outline=(34, 197, 94, 160), width=2)
    draw.text((sx1 + margin + 24, cal_y + 24), "28-DAY STREAK & SHIELD ARCHIVE", font=FONT_BODY_BOLD, fill=(251, 191, 36))
    draw.text((sx1 + margin + 24, cal_y + 60), "2 Streak Freezes Banked • Tap any day to replay", font=FONT_BODY_SMALL, fill=(148, 163, 184))
    
    cal_start_x = sx1 + margin + 24
    cal_start_y = cal_y + 110
    cal_cell_w = (content_w - 48 - 6 * 14) // 7
    cal_cell_h = 76
    cgap = 14
    
    for day in range(1, 29):
        c_idx = (day - 1) % 7
        r_idx = (day - 1) // 7
        cx = cal_start_x + c_idx * (cal_cell_w + cgap)
        cy = cal_start_y + r_idx * (cal_cell_h + cgap)
        
        if day == 15:
            draw.rounded_rectangle([cx, cy, cx + cal_cell_w, cy + cal_cell_h], radius=14, fill=(30, 58, 138))
            draw.text((cx + cal_cell_w // 2, cy + cal_cell_h // 2 - 10), str(day), font=FONT_BODY_BOLD, fill=(255, 255, 255), anchor="mm")
            draw.text((cx + cal_cell_w // 2, cy + cal_cell_h // 2 + 16), "SHIELD", font=FONT_KEY_SPEC, fill=(147, 197, 253), anchor="mm")
        elif day <= 22:
            draw.rounded_rectangle([cx, cy, cx + cal_cell_w, cy + cal_cell_h], radius=14, fill=COLOR_GREEN_FACE)
            draw.text((cx + cal_cell_w // 2, cy + cal_cell_h // 2), str(day), font=FONT_BODY_BOLD, fill=(255, 255, 255), anchor="mm")
        else:
            draw.rounded_rectangle([cx, cy, cx + cal_cell_w, cy + cal_cell_h], radius=14, fill=(30, 41, 59))
            draw.text((cx + cal_cell_w // 2, cy + cal_cell_h // 2), str(day), font=FONT_BODY_BOLD, fill=(148, 163, 184), anchor="mm")

    callout_y = cal_y + 570
    draw.rounded_rectangle([sx1 + margin, callout_y, sx2 - margin, callout_y + 66], radius=16, fill=(30, 41, 59, 140))
    draw.text((WIDTH // 2, callout_y + 33), "Full Deduction Breakdown & Replayable History", font=FONT_CALLOUT, fill=(203, 213, 225, 255), anchor="mm")

    img.save("play_store_assets/screenshot_05_lexi_bot_stats.png", "PNG", optimize=True)
    img.save("play_store_assets/screenshot_05_bot_stats.png", "PNG", optimize=True)
    print("Screenshot 5 saved.")

# 6. Themes & Settings
def make_screenshot_6():
    img, draw, screen = create_base_canvas(
        headline="CUSTOM THEMES & HAPTICS",
        subtitle="6 Handcrafted Palettes & Tactile Sensory Controls",
        ambient_color=(168, 85, 247, 40)
    )
    sx1, sy1, sx2, sy2 = screen
    margin = 36
    content_w = (sx2 - sx1) - margin * 2
    
    draw.text((sx1 + margin, sy1 + 50), "Themes & Customization", font=FONT_PHONE_TITLE, fill=(255, 255, 255, 255))
    
    themes = [
        ("Classic Emerald", [(46, 125, 50), (245, 124, 0), (51, 65, 85)]),
        ("Obsidian Night", [(234, 179, 8), (148, 163, 184), (30, 41, 59)]),
        ("Cyberpunk Neon", [(6, 182, 212), (236, 72, 153), (30, 27, 75)]),
        ("Rose Gold", [(244, 63, 94), (251, 146, 60), (76, 29, 149)]),
    ]
    card_w = (content_w - 20) // 2
    card_h = 240
    card_gap = 20
    
    for idx, (tname, colors) in enumerate(themes):
        col = idx % 2
        row = idx // 2
        bx = sx1 + margin + col * (card_w + card_gap)
        by = sy1 + 120 + row * (card_h + card_gap)
        
        draw.rounded_rectangle([bx, by, bx + card_w, by + card_h], radius=20, fill=(15, 23, 42, 220), outline=(168, 85, 247, 160) if idx == 0 else (51, 65, 85, 255), width=2)
        draw.text((bx + 20, by + 20), tname, font=FONT_BODY_BOLD, fill=(255, 255, 255))
        
        for cidx, c in enumerate(colors):
            cx = bx + 24 + cidx * 56
            cy = by + 120
            draw.ellipse([cx, cy, cx + 44, cy + 44], fill=c)
            
        if idx == 0:
            draw.text((bx + 20, by + 190), "✓ ACTIVE PALETTE", font=FONT_BODY_SMALL, fill=(168, 85, 247))

    settings_y = sy1 + 120 + 2 * (card_h + card_gap) + 30
    draw.rounded_rectangle([sx1 + margin, settings_y, sx2 - margin, settings_y + 420], radius=22, fill=(15, 23, 42, 220), outline=(51, 65, 85, 255), width=2)
    draw.text((sx1 + margin + 24, settings_y + 24), "TACTILE SENSORY CONTROLS", font=FONT_BODY_BOLD, fill=(148, 163, 184))
    
    toggles = [
        ("Haptic Feedback Travel", "Crisp mechanical motor response on every key press", "ON"),
        ("Procedural Synthesizer Audio", "Zero-allocation pitch synthesizer acoustic clicks", "ON"),
        ("3D Gyroscope Parallax Tilt", "Real-time hardware depth perspective shifts", "ON"),
    ]
    ty = settings_y + 80
    for title, desc, status in toggles:
        draw.text((sx1 + margin + 24, ty), title, font=FONT_BODY_BOLD, fill=(255, 255, 255))
        draw.text((sx1 + margin + 24, ty + 36), desc, font=FONT_BODY_SMALL, fill=(148, 163, 184))
        draw.rounded_rectangle([sx2 - margin - 100, ty + 10, sx2 - margin - 24, ty + 50], radius=20, fill=(46, 125, 50))
        draw.text((sx2 - margin - 62, ty + 30), status, font=FONT_KEY_SPEC, fill=(255, 255, 255), anchor="mm")
        ty += 110

    callout_y = settings_y + 450
    draw.rounded_rectangle([sx1 + margin, callout_y, sx2 - margin, callout_y + 66], radius=16, fill=(30, 41, 59, 140))
    draw.text((WIDTH // 2, callout_y + 33), "Customize Board Lighting, Acoustics & Haptics", font=FONT_CALLOUT, fill=(203, 213, 225, 255), anchor="mm")

    img.save("play_store_assets/screenshot_06_themes_settings.png", "PNG", optimize=True)
    print("Screenshot 6 saved.")

if __name__ == "__main__":
    make_screenshot_1()
    make_screenshot_2()
    make_screenshot_3()
    make_screenshot_4()
    make_screenshot_5()
    make_screenshot_6()
    print("All 6 screenshots regenerated successfully!")
