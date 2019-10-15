import * as React from 'react'
import {
  Alignment,
  AnchorButton,
  Navbar,
  NavbarGroup,
  NavbarHeading,
  NavbarDivider, Tooltip,
} from '@blueprintjs/core'
import { BaseComponent } from '../basis/BaseComponent'
import { Broadcast } from '../basis/Broadcast'

interface INavigationState {
  isConsoleOpen: boolean
  isDark: boolean
}

export class Navigation extends BaseComponent<any, INavigationState> {
  constructor(props: any) {
    super(props)
    this.state = {
      isConsoleOpen: Broadcast.isConsoleOpen(),
      isDark: Broadcast.isDark(),
    }
  }

  public render() {
    return (
      <Navbar>
        <NavbarGroup align={Alignment.LEFT}>
          <NavbarHeading>UERANSIM</NavbarHeading>
          <NavbarDivider/>
          <Tooltip content={`${this.state.isConsoleOpen ? 'Hide' : 'Show'} Console`}>
            <AnchorButton
              minimal={true}
              rightIcon="console"
              onClick={() => this.handleConsoleClick()}
              // active={this.state.isConsoleOpen}
            />
          </Tooltip>
          <Tooltip content={`Switch to ${this.state.isDark ? 'Light' : 'Dark'} Theme`}>
            <AnchorButton
              minimal={true}
              rightIcon={this.state.isDark ? 'flash' : 'moon'}
              onClick={() => Broadcast.setDark(!Broadcast.isDark())}
            />
          </Tooltip>
        </NavbarGroup>
      </Navbar>
    )
  }

  handleConsoleClick() {
    const open = !this.state.isConsoleOpen
    this.setState({ isConsoleOpen: open })
    Broadcast.setConsoleOpen(open)
  }

  onThemeChanged(isDark: boolean): void {
    this.setState({ isDark: isDark })
  }
}
